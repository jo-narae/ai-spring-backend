package com.sesac.aibackend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sesac.aibackend.error.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 인증 실패 시 ErrorResponse 표준 포맷으로 401 응답.
 *
 * 미설정 시 Spring 기본 HTML 페이지가 반환되어 GlobalExceptionHandler와 응답 포맷이 어긋납니다.
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Override
    public void commence(HttpServletRequest request,
                          HttpServletResponse response,
                          AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ErrorResponse body = ErrorResponse.of("UNAUTHORIZED", authException.getMessage());
        objectMapper.writeValue(response.getWriter(), body);
    }
}
