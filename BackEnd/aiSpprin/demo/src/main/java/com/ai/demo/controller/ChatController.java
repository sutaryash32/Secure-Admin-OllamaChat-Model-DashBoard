package com.ai.demo.controller;

import com.ai.demo.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Map<String, String>> chat(@RequestParam String prompt) {
        return chatService.streamResponse(prompt)
                .map(token -> Map.of("content", token))
                .concatWith(Flux.just(Map.of("content", "[DONE]")));
    }

    @GetMapping(value = "/cricket", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Map<String, String>> cricket(@RequestParam String prompt) {
        return chatService.streamCricketResponse(prompt)
                .map(token -> Map.of("content", token))
                .concatWith(Flux.just(Map.of("content", "[DONE]")));
    }
}
