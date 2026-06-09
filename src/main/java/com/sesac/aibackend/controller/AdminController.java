package com.sesac.aibackend.controller;

import com.sesac.aibackend.domain.User;
import com.sesac.aibackend.dto.RoleUpdateRequest;
import com.sesac.aibackend.repository.UserRepository;
import com.sesac.aibackend.service.AdminService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 관리자 전용 사용자 관리 라우트 (Day 5 역할 권한 시연).
 *
 * SecurityConfig의 requestMatchers("/admin/**").hasRole("ADMIN")와
 * 메서드 단의 @PreAuthorize("hasRole('ADMIN')") 양쪽으로 이중 보호합니다.
 *
 * 시드 계정: admin / admin1234 (DataInitializer, dev 프로파일 한정)
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final AdminService adminService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public List<Map<String, Object>> listUsers() {
        return adminService.findAll().stream()
                .map(this::summarize)
                .toList();
    }

    /**
     * 사용자 역할 변경.
     *
     * @Pattern 위반(USER/ADMIN 외 값)은 GlobalExceptionHandler가 400으로,
     * 없는 id는 NotFoundException → 404로 처리합니다.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/users/{id}/role")
    public Map<String, Object> changeRole(
            @PathVariable Long id,
            @Valid @RequestBody RoleUpdateRequest req) {
        return summarize(adminService.changeRole(id, req.role()));
    }

    /**
     * 사용자 삭제.
     *
     * 연관 ChatLog는 서비스 계층에서 선삭제합니다(FK 제약). 성공 시 204.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    private Map<String, Object> summarize(User u) {
        return Map.of(
                "id", u.getId(),
                "username", u.getUsername(),
                "role", u.getRole()
        );
    }
}
