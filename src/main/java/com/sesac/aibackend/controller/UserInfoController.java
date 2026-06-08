package com.sesac.aibackend.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 인증된 사용자 정보 조회 (Day 4 B6).
 *
 * principal 타입을 UserDetails로 통일했기 때문에, 폼 로그인이든 구글 OAuth 로그인이든
 * 동일하게 @AuthenticationPrincipal로 인증 사용자를 주입받을 수 있습니다.
 */
@RestController
public class UserInfoController {

    @GetMapping("/me")
    public Map<String, String> me(@AuthenticationPrincipal UserDetails user) {
        return Map.of("username", user.getUsername());
    }
}
