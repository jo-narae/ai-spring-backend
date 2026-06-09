package com.sesac.aibackend.service;

import com.sesac.aibackend.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * FastAPI(/chat) 호출 클라이언트.
 *
 * 비동기 Mono를 반환하지만 컨트롤러에서 block()으로 동기 호출도 가능합니다.
 * 초보자 친화를 위해 ChatController는 block()으로 시작하고 비동기는 옵션으로 안내합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PythonChatClient {

    private final WebClient pythonWebClient;

    public Mono<ChatResponse> chat(String prompt) {
        Map<String, Object> body = Map.of("prompt", prompt);
        return pythonWebClient.post()
                .uri("/chat")
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, resp -> {
                    log.warn("python 4xx: {}", resp.statusCode());
                    return Mono.error(new IllegalArgumentException("invalid request to python"));
                })
                .onStatus(HttpStatusCode::is5xxServerError, resp -> {
                    log.error("python 5xx: {}", resp.statusCode());
                    return Mono.error(new RuntimeException("python server error"));
                })
                .bodyToMono(ChatResponse.class);
    }
}
