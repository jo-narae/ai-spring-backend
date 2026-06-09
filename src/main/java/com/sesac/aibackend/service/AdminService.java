package com.sesac.aibackend.service;

import com.sesac.aibackend.domain.ChatLog;
import com.sesac.aibackend.domain.Role;
import com.sesac.aibackend.domain.User;
import com.sesac.aibackend.error.NotFoundException;
import com.sesac.aibackend.repository.ChatLogRepository;
import com.sesac.aibackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 관리자 전용 사용자 관리 서비스.
 *
 * 트랜잭션 경계를 서비스 메서드 단위로 명시합니다(ChatLogService와 동일한 패턴).
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final ChatLogRepository chatLogRepository;

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    /**
     * 역할 변경.
     *
     * 영속 엔티티의 도메인 메서드를 호출하면 트랜잭션 종료 시 더티 체킹으로 반영됩니다.
     */
    @Transactional
    public User changeRole(Long id, Role role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("user", id));
        user.changeRole(role);
        return user;
    }

    /**
     * 사용자 삭제.
     *
     * ChatLog.user_id는 notnull FK(단방향 ManyToOne)이므로
     * 로그를 보유한 사용자를 바로 삭제하면 FK 제약을 위반합니다.
     * 따라서 연관 로그를 먼저 삭제한 뒤 사용자를 삭제합니다.
     */
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("user", id));

        List<ChatLog> logs = chatLogRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        if (!logs.isEmpty()) {
            chatLogRepository.deleteAll(logs);
        }
        userRepository.delete(user);
    }
}
