package com.sesac.aibackend.controller;

import com.sesac.aibackend.domain.ChatLog;
import com.sesac.aibackend.dto.ChatLogRequest;
import com.sesac.aibackend.dto.ChatLogResponse;
import com.sesac.aibackend.service.ChatLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/chat-logs")
@RequiredArgsConstructor
public class ChatLogController {

    private final ChatLogService chatLogService;

    /**
     * [getId 버전] userId(PK)로 대화 로그를 최신순으로 조회합니다.
     * fetch join 없이 from()을 사용 → username은 응답에 null.
     * 예) GET /chat-logs?userId=1
     */
    @GetMapping
    public List<ChatLogResponse> list(@RequestParam Long userId) {
        return chatLogService.findByUserId(userId).stream()
                .map(ChatLogResponse::from)
                .toList();
    }

    /**
     * [getName 버전] userId(PK)로 조회하며 fetch join으로 user를 함께 로딩해 username까지 응답합니다.
     * 트랜잭션이 닫힌 뒤에도 getUser().getUsername() 접근이 안전합니다.
     * 예) GET /chat-logs/with-user?userId=1
     */
    @GetMapping("/with-user")
    public List<ChatLogResponse> listWithUser(@RequestParam Long userId) {
        return chatLogService.findByUserIdWithUser(userId).stream()
                .map(ChatLogResponse::fromWithUsername)
                .toList();
    }

    @PostMapping
    public ResponseEntity<ChatLogResponse> create(@Valid @RequestBody ChatLogRequest req) {
        ChatLog saved = chatLogService.save(req.userId(), req.prompt(), req.response());
        URI location = URI.create("/chat-logs/" + saved.getId());
        return ResponseEntity.created(location).body(ChatLogResponse.from(saved));
    }
}
