package com.sesac.aibackend.controller;

import com.sesac.aibackend.domain.Role;
import com.sesac.aibackend.domain.User;
import com.sesac.aibackend.dto.LoginRequest;
import com.sesac.aibackend.dto.SignupRequest;
import com.sesac.aibackend.error.DuplicateException;
import com.sesac.aibackend.repository.UserRepository;
import com.sesac.aibackend.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 인증 컨트롤러 — /signup, /login.
 *
 * /login 성공 시 JWT를 발급하여 응답합니다.
 *
 * 동시성 주의: existsByUsername 후 save 는 TOCTOU race 위험.
 * users.username UNIQUE 제약과 DataIntegrityViolationException 잡기로 이중 방어합니다.
 */
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signup(@Valid @RequestBody SignupRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new DuplicateException("username already taken: " + req.username());
        }
        try {
            userRepository.save(User.builder()
                    .username(req.username())
                    .passwordHash(passwordEncoder.encode(req.password()))
                    .role(Role.USER)
                    .build());
        } catch (DataIntegrityViolationException e) {
            // unique 제약 위반 — 동시 가입 시도
            throw new DuplicateException("username already taken: " + req.username());
        }
        return ResponseEntity.status(201).body(Map.of("username", req.username()));
    }

    @PostMapping("/login")
    public Map<String, String> login(@Valid @RequestBody LoginRequest req) {
        Authentication auth = authenticationManager.authenticate( // 검증 단계 -> 내부적으로는 UserDetailsService의 loadUserByUsername 동작
                new UsernamePasswordAuthenticationToken(req.username(), req.password())
        );
        String role = auth.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .map(a -> a.replace("ROLE_", ""))
                .orElse("USER");
        String token = jwtUtil.generate(auth.getName(), role);
        return Map.of("token", token, "username", auth.getName(), "role", role);
    }
}
