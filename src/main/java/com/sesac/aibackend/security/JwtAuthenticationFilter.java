package com.sesac.aibackend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 모든 요청에서 1회 실행되어 Authorization 헤더의 JWT를 검증합니다.
 *
 * 검증 성공 시 UserDetails 를 principal 로 세팅하여 컨트롤러에서
 * {@code @AuthenticationPrincipal UserDetails user} 로 받을 수 있도록 합니다.
 * (Form 로그인 경로와 principal 타입 일관성을 유지합니다.)
 */
@Slf4j
@Component // 의존성 주입
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // Bearer type: 신원 인증 방법
    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    // 생성 검증 객체, 의존성 주입을 받아옴.
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain chain) throws ServletException, IOException {
        // get header 읽어오기
        String header = request.getHeader(HEADER);

        if (header != null && header.startsWith(PREFIX)) {
            String token = header.substring(PREFIX.length()); //Bearer 제외
            try {
                Claims claims = jwtUtil.parse(token); // 토큰 검증
                String username = claims.getSubject(); // 사용자 이름
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                // userDetail 객체 생성

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                // security에서 관리하는 user 객체
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (JwtException e) {
                log.debug("JWT verification failed: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            } catch (UsernameNotFoundException e) {
                log.debug("user from JWT not found: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }
}
