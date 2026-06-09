package com.sesac.aibackend.controller;

import com.sesac.aibackend.dto.ChatLogResponse;
import com.sesac.aibackend.dto.ChatRequest;
import com.sesac.aibackend.dto.ChatResponse;
import com.sesac.aibackend.service.ChatLogService;
import com.sesac.aibackend.service.PythonChatClient;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * /chat 프록시 컨트롤러 (Day 5).
 *
 * 흐름: 인증된 사용자 → FastAPI 호출 → 응답을 ChatLog로 저장 → 응답 반환.
 *
 * 초보자 친화를 위해 block()으로 동기 호출합니다.
 * 주의: WebFlux 컨텍스트에서 block()을 호출하면 데드락이 발생합니다.
 * Spring MVC(서블릿) 컨텍스트에서는 워커 스레드가 분리되어 있어 안전합니다.
 *
 * 운영 권장은 {@code Mono<ChatResponse>} 반환입니다.
 *
 * 트랜잭션 경계: FastAPI 호출 후 별도 트랜잭션으로 ChatLog 저장.
 * 외부 호출과 DB 저장의 원자성은 보장되지 않습니다 (응답은 나갔는데 로그 누락 가능).
 */
@Slf4j
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ChatController {

    private final PythonChatClient pythonClient;
    private final ChatLogService chatLogService;

    @PostMapping
    public ChatResponse chat(
            @Valid @RequestBody ChatRequest req,
            @AuthenticationPrincipal UserDetails user) {

        String username = user.getUsername();
        ChatResponse response = pythonClient.chat(req.prompt()).block();
        if (response == null) {
            throw new IllegalStateException("python returned null");
        }

        chatLogService.save(username, req.prompt(), response.answer());

        return response;
    }
}
