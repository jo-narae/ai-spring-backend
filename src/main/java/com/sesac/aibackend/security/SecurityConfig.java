package com.sesac.aibackend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration //bean으로 등록
@EnableWebSecurity //security chain 관할 e.g. authorize http request
@EnableMethodSecurity // security 설정
@RequiredArgsConstructor // 의존성 주입 final_생성자 주입
//
public class SecurityConfig {
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CorsConfigurationSource corsConfigurationSource;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 인증 실패(401) + 권한 부족(403)을 모두 ErrorResponse JSON 표준 포맷으로 통일
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        // 인증 불필요. /error는 403 에러 포워드가 막혀 401로 덮이지 않도록 개방
                        .requestMatchers(
                                "/login", "/signup", "/health",
                                "/oauth2/**", "/login/oauth2/**",
                                "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**",
                                "/h2-console/**", "/error",
                                "/swagger-ui"
                        ).permitAll()
                        // Day 2 인메모리 CRUD 학습용 (운영 권장 X)
                        .requestMatchers("/legacy/items/**").permitAll()
                        // 관리자 전용
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // 그 외 모두 인증 필요 (Day 3 JPA CRUD, /chat 등)
                        .anyRequest().authenticated()
                )
                // 구글 OAuth2 로그인 — 성공 시 핸들러가 앱 JWT를 발급 (Day 4 B7/B8)
                // 인가 시작: GET /oauth2/authorization/google, 콜백: /login/oauth2/code/google (자동)
                // 처음엔 .oauth2Login(Customizer.withDefaults()) 이걸로 합니다. 핸들러는 다음 파트에서 진행됩니다.
                .oauth2Login(oauth2 -> oauth2.successHandler(oAuth2LoginSuccessHandler))

                // H2 콘솔 사용을 위한 헤더 완화 (개발 프로파일만)
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}
