package com.sesac.aibackend.dto;

import com.sesac.aibackend.domain.Role;
import jakarta.validation.constraints.NotNull;

/**
 * 관리자의 사용자 역할 변경 요청.
 *
 * 허용 값은 Role enum(USER, ADMIN)으로 강제됩니다.
 * - 허용 값 외(예: "FOO")는 Jackson 역직렬화 단계에서 실패합니다
 *   → GlobalExceptionHandler의 HttpMessageNotReadableException 핸들러가 400으로 응답.
 * - null/필드 누락은 @NotNull로 잡혀 VALIDATION_FAILED(400)로 응답.
 */
public record RoleUpdateRequest(
        @NotNull Role role
) {}
