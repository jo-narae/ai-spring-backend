package com.sesac.aibackend.config;

import com.sesac.aibackend.domain.ChatLog;
import com.sesac.aibackend.domain.Role;
import com.sesac.aibackend.domain.User;
import com.sesac.aibackend.repository.ChatLogRepository;
import com.sesac.aibackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 개발 프로파일에서 부팅 시 시드 데이터를 적재합니다.
 *
 * - admin / admin1234 (ROLE_ADMIN)
 * - alice / password123 (ROLE_USER) + 샘플 ChatLog 3건
 *
 * 운영 프로파일에서는 비활성화됩니다(@Profile("dev")).
 */
@Slf4j
@Configuration
@Profile("dev")
@RequiredArgsConstructor
public class DataInitializer {

    @Bean
    public ApplicationRunner seedData(
            UserRepository userRepository,
            ChatLogRepository chatLogRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            if (userRepository.count() > 0) {
                log.info("seed skipped: users already exist");
                return;
            }

            User admin = userRepository.save(User.builder()
                    .username("admin")
                    .passwordHash(passwordEncoder.encode("admin1234"))
                    .role(Role.ADMIN)
                    .build());

            User alice = userRepository.save(User.builder()
                    .username("alice")
                    .passwordHash(passwordEncoder.encode("password123"))
                    .role(Role.USER)
                    .build());

            chatLogRepository.save(ChatLog.builder()
                    .user(alice)
                    .prompt("FastAPI를 한 줄로 설명해 주세요")
                    .response("Python 타입 힌트 기반의 비동기 친화 웹 프레임워크입니다.")
                    .build());
            chatLogRepository.save(ChatLog.builder()
                    .user(alice)
                    .prompt("Spring Boot 3.5 새 기능은?")
                    .response("Spring Framework 6.2 통합과 Virtual Threads 안정화가 핵심입니다.")
                    .build());
            chatLogRepository.save(ChatLog.builder()
                    .user(alice)
                    .prompt("JPA의 영속성 컨텍스트가 뭔가요?")
                    .response("같은 트랜잭션 내 동일 식별자 객체를 보장하는 1차 캐시입니다.")
                    .build());

            log.info("seed loaded: admin={}, alice={}, chatLogs=3", admin.getId(), alice.getId());
        };
    }
}
