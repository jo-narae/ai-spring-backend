package com.sesac.aibackend.repository;

import com.sesac.aibackend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    /** 소셜 로그인 신원 조회 — (provider, providerId) 조합이 사용자의 안정적 식별 키입니다. */
    Optional<User> findByProviderAndProviderId(String provider, String providerId);
}
