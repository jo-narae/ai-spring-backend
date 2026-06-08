package com.sesac.aibackend.security;

import com.sesac.aibackend.domain.User;
import com.sesac.aibackend.repository.UserRepository;
//add below 2
import com.sesac.aibackend.security.oauth2.OAuth2UserInfo;
import com.sesac.aibackend.security.oauth2.OAuth2UserInfoFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
// add
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 구글 OAuth2 로그인 성공 직후를 가로채는 핸들러 (Day 4 B8).
 *
 * 흐름: 구글 인증 성공 → OIDC 사용자 정보로 우리 DB 사용자를 조회/생성 →
 * 폼 로그인과 동일한 방식으로 앱 자체 JWT를 발급 → 프런트로 토큰을 붙여 리다이렉트.
 * 인증 출처(폼/구글)가 달라도 이후 API는 동일한 앱 JWT로 동작합니다.
 */
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    /** 토큰을 전달할 프런트 콜백 주소 (기본값은 React 개발 서버). */
    @Value("${app.oauth2.redirect-uri:http://localhost:5173/oauth/callback}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        //OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
        //String email = oidcUser.getEmail();
        // OIDC sub: 구글이 보증하는 불변 고유 식별자. 이메일과 달리 변경·재사용되지 않습니다.
        //String providerId = oidcUser.getSubject();

        /*
         * OAuth2 로그인 성공 후 호출되는 핸들러입니다.
         *
         * Spring Security가 authorization code 교환, access token 발급,
         * 사용자 정보 조회까지 완료한 뒤 이 메서드를 호출합니다.
         * 여기서는 외부 OAuth 인증 결과를 우리 서비스의 User/JWT 체계로 변환합니다.
         */
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        String registrationId = oauthToken.getAuthorizedClientRegistrationId();
        /*
         * provider마다 사용자 정보 구조가 다르기 때문에 Factory를 통해 공통 인터페이스로 변환합니다.
         *
         * Google: OIDC 기반 OidcUser
         * Kakao : 일반 OAuth2User + 중첩 attributes 구조
         *
         * 이후 로직은 OAuth2UserInfo 인터페이스만 사용하므로 provider별 분기 코드를 줄일 수 있습니다.
         */
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.create(registrationId, authentication);

        // (provider, providerId)로 우리 DB 사용자를 조회하거나, 처음이면 신규 생성
        /*
         * OAuth 사용자는 email이 아니라 provider + providerId로 식별합니다.
         *
         * Kakao의 경우 account_email scope를 요청하지 않거나 사용자가 동의하지 않으면
         * email이 null일 수 있습니다. 따라서 email을 필수 식별자로 사용하면
         * DB 저장 또는 JWT 생성 과정에서 500 에러가 발생할 수 있습니다.
         */
        User user = userRepository.findByProviderAndProviderId(
                        userInfo.getProvider(),
                        userInfo.getProviderId()
                )
                .orElseGet( () -> {
                    /*
                     * email이 제공되면 username으로 사용하고,
                     * email이 없으면 provider_providerId 형태의 내부 username을 생성합니다.
                     *
                     * 예:
                     * kakao_123456789
                     *
                     * 이렇게 하면 email 동의항목 없이도 사용자를 안정적으로 생성할 수 있습니다.
                     */
                    String username = userInfo.getEmail();
                    if(username == null || username.isBlank()){
                        username = userInfo.getProvider() + "_" + userInfo.getProviderId();
                    }
                    return userRepository.save(User.oauthUser(
                            username,
                            userInfo.getProvider(),
                            userInfo.getProviderId()
                            )

                    );
                        });

        // 폼 로그인과 동일한 방식으로 앱 자체 JWT 발급
        /*
         * 외부 OAuth 로그인 성공 후에도 우리 서비스 API 인증은 자체 JWT로 통일합니다.
         * 따라서 폼 로그인 사용자와 OAuth 로그인 사용자가 동일한 방식으로 API를 호출할 수 있습니다.
         */
        String token = jwtUtil.generate(user.getUsername(), user.getRole().name());

        // SPA라면 토큰을 프런트로 전달하여 리다이렉트
        /*
         * 발급한 JWT를 프론트엔드 callback 페이지로 전달합니다.
         * 프론트는 token을 저장한 뒤 이후 API 요청의 Authorization 헤더에 사용합니다.
         */
        response.sendRedirect(redirectUri + "?token=" + token);

        // debugging 용: 카카오 실행 시 실제로 email이 null인것을 확인 가능.
        System.out.println("registrationId = " + registrationId);
        System.out.println("provider = " + userInfo.getProvider());
        System.out.println("providerId = " + userInfo.getProviderId());
        System.out.println("email = " + userInfo.getEmail());
        System.out.println("principal = " + authentication.getPrincipal());
    }
}
