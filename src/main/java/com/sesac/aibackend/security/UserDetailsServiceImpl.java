package com.sesac.aibackend.security;

import com.sesac.aibackend.domain.User;
import com.sesac.aibackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Spring Security의 사용자 조회 어댑터.
 *
 * 인증 매니저가 로그인 시 이 서비스를 호출하여 비밀번호 검증을 수행합니다.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        // Spring Security의 ROLE_ 접두사 컨벤션
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole());

        // 소셜 로그인 사용자는 passwordHash가 NULL입니다. Spring의 User는 password에 null을
        // 허용하지 않으므로, BCrypt.matches가 절대 통과하지 못하는 빈 문자열로 대체합니다.
        // (JWT 인증 경로에서는 비밀번호 검증을 하지 않으므로 무관하고, 폼 로그인 시도는 항상 실패합니다.)
        String password = user.getPasswordHash() != null ? user.getPasswordHash() : "";

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                password,
                List.of(authority)
        );
    }
}
