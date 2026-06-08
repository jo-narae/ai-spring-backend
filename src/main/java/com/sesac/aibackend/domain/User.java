package com.sesac.aibackend.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 로그인 아이디 (유일)
     */
    @Column(
            unique = true,
            nullable = false,
            length = 100
    )
    private String username;

    /**
     * BCrypt 해시 (Day4에서 사용)
     */
    @Column(length = 200)
    private String passwordHash;

    /**
     * USER / ADMIN
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    /** 인증 출처: "LOCAL"(폼 가입) 또는 "GOOGLE"(소셜 로그인). */
    @Builder.Default
    @Column(nullable = false, length = 20)
    private String provider = "LOCAL";

    /** 소셜 공급자의 안정적 고유 식별자(OIDC sub). LOCAL 사용자는 NULL. */
    @Column(name = "provider_id", length = 255)
    private String providerId;

    /**
     * 소셜(OAuth2) 로그인 사용자 생성 팩토리.
     *
     * 로컬 비밀번호가 없으므로 passwordHash는 NULL로 두고,
     * 구글이 보증하는 불변 식별자(sub)를 providerId에 저장합니다.
     * 이로써 이 계정은 폼 로그인(/login)으로는 인증될 수 없고 구글 로그인만 가능합니다.
     */
    public static User oauthUser(String email, String providerId) {
        return User.builder()
                .username(email)
                .passwordHash(null)
                .role(Role.USER)
                .provider("GOOGLE")
                .providerId(providerId)
                .build();
    }
}
