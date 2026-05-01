package com.PAWPAW.pawpaw.ai.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    // بنعرف الـ builder بس هنا
    private final WebClient.Builder webClientBuilder = WebClient.builder();

    @PostMapping("/predict")
    public Mono<String> predict(@RequestBody Map<String, String> request) {
        String message = request.get("description");

        Map<String, Object> body = Map.of(
                "model", "google/gemini-2.0-flash-001",
                "messages", List.of(Map.of("role", "user", "content", message))
        );

        return webClientBuilder.build()
                .post()
                .uri("https://openrouter.ai/api/v1/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> Mono.just("AI Error: " + e.getMessage()));
    }
}