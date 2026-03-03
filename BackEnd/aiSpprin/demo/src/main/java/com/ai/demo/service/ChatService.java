package com.ai.demo.service;

import reactor.core.publisher.Flux;

public interface ChatService {
    Flux<String> streamResponse(String prompt);
    Flux<String> streamCricketResponse(String prompt);
}