package com.sesac.aibackend.dto;

import com.sesac.aibackend.domain.Role;
import com.sesac.aibackend.domain.User;
import jakarta.validation.constraints.NotBlank;

public record UserRequest(
        @NotBlank String username,
        @NotBlank String password
) {
    public User toEntity() {
        return User.builder()
                .username(username)
                // TODO Day4: 평문 저장 대신 BCrypt 해시로 교체
                .passwordHash(password)
                // 가입은 항상 USER로 고정. 클라이언트가 role(ADMIN 등)을 지정하지 못하게 해
                // 권한 상승을 차단합니다. 관리자 승격은 별도 경로로만 처리합니다.
                .role(Role.USER)
                .build();
    }
}
