package com.ai.demo.service;

import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Random;

@Service
public class ChatServiceImpl implements ChatService {

    private final OllamaChatModel ollamaChatModel;
    private final Random random = new Random();

    private static final SystemMessage GENERAL_SYSTEM = new SystemMessage(
            "You are a helpful assistant. Keep your responses short, concise and to the point. " +
                    "Maximum 3-4 sentences. No long paragraphs."
    );

    private static final SystemMessage CRICKET_SYSTEM = new SystemMessage(
            "You are a cricket expert assistant. Keep your responses short, concise and to the point. " +
                    "Maximum 3-4 sentences. No long paragraphs. Only answer cricket-related questions."
    );

    private static final List<String> OUT_OF_SYLLABUS_RESPONSES = List.of(
            "Hmm… that's outside my syllabus!",
            "I'm trained only for cricket talk 🏏",
            "Try asking me about IPL, wickets, or scores!",
            "Oops, that's not cricket-related!",
            "Stick to cricket and I'll shine!"
    );

    public ChatServiceImpl(OllamaChatModel ollamaChatModel) {
        this.ollamaChatModel = ollamaChatModel;
    }

    @Override
    public Flux<String> generateResponseReactive(String prompt) {
        return ollamaChatModel.stream(new Prompt(List.of(GENERAL_SYSTEM, new UserMessage(prompt))))
                .map(response -> response.getResult().getOutput().getText())
                .filter(text -> text != null && !text.isEmpty());
    }

    @Override
    public Flux<String> generateCricketResponseReactive(String prompt) {
        return isCricketRelatedDynamic(prompt)
                .cache() // ✅ prevent multiple subscriptions
                .flatMapMany(isCricket -> {
                    if (!isCricket) {
                        String reply = OUT_OF_SYLLABUS_RESPONSES.get(
                                random.nextInt(OUT_OF_SYLLABUS_RESPONSES.size())
                        );
                        return Flux.just(reply);
                    }

                    return ollamaChatModel.stream(new Prompt(List.of(CRICKET_SYSTEM, new UserMessage(prompt))))
                            .map(resp -> resp.getResult().getOutput().getText())
                            .filter(text -> text != null && !text.isEmpty());
                });
    }

    private Mono<Boolean> isCricketRelatedDynamic(String prompt) {
        String classificationPrompt = """
                You are a strict topic classifier. Your job is to determine if a query is about cricket.

                Rules:
                - Reply with ONLY the single word: YES or NO
                - Do not add any explanation, punctuation, or extra words
                - YES = the query is about cricket (the sport)
                - NO = the query is not about cricket

                Query: %s

                Answer (YES or NO only):
                """.formatted(prompt);

        return ollamaChatModel.stream(new Prompt(new UserMessage(classificationPrompt)))
                .map(resp -> resp.getResult().getOutput().getText())
                .filter(text -> text != null && !text.isEmpty())
                .take(5)
                .collectList()
                .map(parts -> {
                    String fullResponse = String.join("", parts).trim().toUpperCase();
                    System.out.println("Classifier response: " + fullResponse); // ✅ debug log
                    return fullResponse.contains("YES");
                });
    }
}
