package com.PAWPAW.pawpaw.ai.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
public class AiService {

    @Value("${SPRING_AI_OPENAI_API_KEY:dummy_key}")
    private String apiKey;

    @Value("${AI_URL:https://openrouter.ai/api/v1/chat/completions}")
    private String aiUrl;

    @Value("${SPRING_AI_OPENAI_CHAT_OPTIONS_MODEL:google/gemini-2.0-flash-exp:free}")
    private String model;

    private final WebClient webClient;

    public AiService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public Mono<String> getAiResponse(String description) {

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(Map.of("role", "user", "content", description))
        );

        return webClient.post()
                .uri(aiUrl)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> Mono.just("AI Service Error: " + e.getMessage()));
    }
}