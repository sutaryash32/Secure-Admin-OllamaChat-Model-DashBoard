package com.ai.demo.Service;

import reactor.core.publisher.Flux;

public interface ChatService {

    public Flux<String> generateResponseReactive(String prompt);

    public Flux<String> generateCricketResponseReactive(String prompt);
}
