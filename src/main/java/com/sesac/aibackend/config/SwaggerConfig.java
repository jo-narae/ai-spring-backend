package com.sesac.aibackend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger UI / OpenAPI 설정 (Day 5).
 *
 * 전역 SecurityRequirement(bearerAuth)를 추가하여 모든 API에 JWT 인증을 표시합니다.
 * 이로써 /swagger-ui.html 우상단 "Authorize" 버튼이 활성화되고, 모든 엔드포인트에
 * 자물쇠가 표시됩니다. (/login, /signup, /health 도 자물쇠가 보이지만 호출에는 영향 없음)
 *
 * "Authorize" 버튼으로 JWT 등록 후 보호 라우트를 호출하실 수 있습니다.
 */
@Configuration
public class SwaggerConfig {

    public static final String SECURITY_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AI Backend Gateway")
                        .version("1.0")
                        .description("Day 2~5 산출물 — Spring Boot 3.5 + Security 6 + JPA + WebClient"))
                // 전역 SecurityRequirement: Swagger UI 우상단 Authorize 버튼으로 JWT 입력 활성화
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
