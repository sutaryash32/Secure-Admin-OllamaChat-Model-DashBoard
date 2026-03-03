package com.ai.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final OllamaChatModel ollamaModel;
    private final Random rng = new Random();

    private static final SystemMessage GENERAL_SYSTEM = new SystemMessage(
            "You are a helpful assistant. Keep responses concise: 3-4 sentences max."
    );
    private static final SystemMessage CRICKET_SYSTEM = new SystemMessage(
            "You are a cricket expert. Keep responses concise: 3-4 sentences max. " +
                    "Only answer cricket-related questions."
    );
    private static final List<String> OFF_TOPIC_REPLIES = List.of(
            "Hmm, that's outside my syllabus!",
            "I'm trained only for cricket talk 🏏",
            "Try asking me about IPL, wickets, or scores!",
            "Oops, that's not cricket-related!"
    );

    @Override
    public Flux<String> streamResponse(String prompt) {
        return ollamaModel
                .stream(new Prompt(List.of(GENERAL_SYSTEM, new UserMessage(prompt))))
                .mapNotNull(r -> r.getResult().getOutput().getText())
                .filter(t -> !t.isBlank());
    }

    @Override
    public Flux<String> streamCricketResponse(String prompt) {
        return isCricketRelated(prompt)
                .cache()
                .flatMapMany(isCricket -> {
                    if (!isCricket) {
                        return Flux.just(OFF_TOPIC_REPLIES.get(rng.nextInt(OFF_TOPIC_REPLIES.size())));
                    }
                    return ollamaModel
                            .stream(new Prompt(List.of(CRICKET_SYSTEM, new UserMessage(prompt))))
                            .mapNotNull(r -> r.getResult().getOutput().getText())
                            .filter(t -> !t.isBlank());
                });
    }

    private Mono<Boolean> isCricketRelated(String prompt) {
        String classifierPrompt = """
            You are a strict topic classifier. Reply ONLY with YES or NO.
            Is the following query about cricket (the sport)?
            Query: %s
            Answer:
            """.formatted(prompt);

        return ollamaModel
                .stream(new Prompt(new UserMessage(classifierPrompt)))
                .mapNotNull(r -> r.getResult().getOutput().getText())
                .filter(t -> !t.isBlank())
                .take(5)
                .collectList()
                .map(parts -> {
                    String result = String.join("", parts).trim().toUpperCase();
                    log.debug("Cricket classifier result for '{}': {}", prompt, result);
                    return result.contains("YES");
                });
    }
}
