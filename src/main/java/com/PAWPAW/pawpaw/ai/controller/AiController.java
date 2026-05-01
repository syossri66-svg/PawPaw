package com.PAWPAW.pawpaw.ai.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Value("${AI_KEY}")
    private String apiKey;

    @Value("${AI_URL}")
    private String aiUrl;

    private final WebClient.Builder webClientBuilder = WebClient.builder();

    @PostMapping("/predict")
    public Mono<String> predict(@RequestBody Map<String, String> request) {
        String message = request.get("description");

        // تجهيز الجسم الخاص بطلب OpenRouter
        Map<String, Object> body = Map.of(
                "model", "google/gemini-2.0-flash-001",
                "messages", List.of(Map.of("role", "user", "content", message))
        );

        return webClientBuilder.build()
                .post()
                .uri(aiUrl)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> Mono.just("AI Service Error: " + e.getMessage()));
    }
}