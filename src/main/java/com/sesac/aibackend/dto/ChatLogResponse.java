package com.sesac.aibackend.dto;

import com.sesac.aibackend.domain.ChatLog;

import java.time.LocalDateTime;

/**
 * 채팅 로그 응답 DTO.
 *
 * user는 LAZY 연관관계라 접근 방식에 따라 동작이 달라집니다. 학습을 위해
 * 두 가지 팩토리 메서드를 함께 제공합니다.
 *
 *  - from()             : getUser().getId()만 사용. FK가 chat_logs에 이미 있어
 *                         프록시를 초기화하지 않으므로 fetch join 없이도 안전.
 *  - fromWithUsername() : getUser().getUsername() 사용. 프록시 초기화가 필요하므로
 *                         조회 시 fetch join(findByUserIdWithUser)으로 트랜잭션 안에서
 *                         user를 미리 로딩해야 LazyInitializationException이 나지 않음.
 */
public record ChatLogResponse(
        Long id,
        Long userId,
        String username,
        String prompt,
        String response,
        LocalDateTime createdAt
) {

    /**
     * getId 버전 — username은 채우지 않습니다(null).
     * LAZY 프록시의 식별자만 읽으므로 fetch join이 필요 없습니다.
     */
    public static ChatLogResponse from(ChatLog chatLog) {
        return new ChatLogResponse(
                chatLog.getId(),
                chatLog.getUser().getId(),
                null,
                chatLog.getPrompt(),
                chatLog.getResponse(),
                chatLog.getCreatedAt()
        );
    }

    /**
     * getName(getUsername) 버전 — fetch join으로 user가 미리 로딩되어 있어야 합니다.
     */
    public static ChatLogResponse fromWithUsername(ChatLog chatLog) {
        return new ChatLogResponse(
                chatLog.getId(),
                chatLog.getUser().getId(),
                chatLog.getUser().getUsername(),
                chatLog.getPrompt(),
                chatLog.getResponse(),
                chatLog.getCreatedAt()
        );
    }
}
