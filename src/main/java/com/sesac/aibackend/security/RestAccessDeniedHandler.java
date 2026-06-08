package com.sesac.aibackend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sesac.aibackend.error.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 권한 부족(인가 실패) 시 ErrorResponse 표준 포맷으로 403 응답.
 *
 * 인증은 됐으나 권한이 없는 요청(예: USER가 /admin 접근)을 필터 레벨에서 가로채,
 * 401 진입점(RestAuthenticationEntryPoint)과 대칭으로 403 JSON을 직접 씁니다.
 *
 * 이 핸들러가 없으면 Spring 기본 AccessDeniedHandlerImpl이 403을 만들지만, 그 응답을 렌더링하려는
 * /error 포워드가 인증을 요구해 401로 덮어써지거나 응답 포맷이 표준과 어긋날 수 있습니다.
 */
@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private static final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ErrorResponse body = ErrorResponse.of("FORBIDDEN", accessDeniedException.getMessage());
        objectMapper.writeValue(response.getWriter(), body);
    }
}
