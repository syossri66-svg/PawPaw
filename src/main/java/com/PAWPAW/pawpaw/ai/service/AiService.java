package com.PAWPAW.pawpaw.ai.service;

import com.PAWPAW.pawpaw.ai.entity.AiScan;
import com.PAWPAW.pawpaw.ai.repository.AiScanRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
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
    private final AiScanRepository aiScanRepository;

    public AiService(WebClient.Builder webClientBuilder, AiScanRepository aiScanRepository) {
        this.webClient = webClientBuilder.build();
        this.aiScanRepository = aiScanRepository;
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

    /**
     * ✅ تعديل الميثود لتعمل بأسلوب صريح ومباشر (Synchronous) متوافق مع الـ JPA
     */
    public AiScan saveAndProcessVisualScan(Long petId, String imageUrl, Long userId) {

        AiScan mockScan = AiScan.builder()
                .userId(userId)
                .petId(petId)
                .imageUrl(imageUrl != null ? imageUrl : "https://pawpaw-app.up.railway.app/uploads/default-pet.jpg")
                .status("COMPLETED")
                .breedDetected("Persian Cat")
                .hasIssue(true)
                .issueName("Feline Dermatitis")
                .confidence(94.2)
                .treatmentTip("Keep the area clean and avoid human soaps. Schedule a clinic visit.")
                .scanDate(LocalDateTime.now())
                .build();


        return aiScanRepository.save(mockScan);
    }

   
    public Long getTotalScansCount() {
        return aiScanRepository.count();
    }
}