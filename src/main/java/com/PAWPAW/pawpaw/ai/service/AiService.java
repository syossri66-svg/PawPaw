package com.PAWPAW.pawpaw.ai.service;

import com.PAWPAW.pawpaw.ai.entity.AiScan;
import com.PAWPAW.pawpaw.ai.repository.AiScanRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class AiService {

    private final WebClient webClient;
    private final AiScanRepository aiScanRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String CHAT_URL    = "https://maghanem-pawpaw-owner-api.hf.space/chat";
    private static final String ANALYZE_URL = "https://maghanem-pawpaw-owner-api.hf.space/analyze";

    public AiService(WebClient.Builder webClientBuilder, AiScanRepository aiScanRepository) {
        this.webClient = webClientBuilder.build();
        this.aiScanRepository = aiScanRepository;
    }


    public Mono<String> getAiResponse(String message) {
        Map<String, String> body = Map.of("message", message);

        return webClient.post()
                .uri(CHAT_URL)
                .header("Content-Type", "application/json")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .map(raw -> {
                    try {
                        JsonNode node = objectMapper.readTree(raw);
                        // حاول تجيب response أو reply أو answer
                        for (String key : new String[]{"response", "reply", "answer", "message"}) {
                            if (node.has(key)) return node.get(key).asText();
                        }
                        return raw;
                    } catch (Exception e) {
                        return raw;
                    }
                })
                .onErrorResume(e -> Mono.just("AI Service Error: " + e.getMessage()));
    }

    public AiScan saveAndProcessVisualScan(Long petId, String imageUrl, Long userId) {
        AiScan scan = AiScan.builder()
                .userId(userId)
                .petId(petId)
                .imageUrl(imageUrl != null ? imageUrl : "")
                .status("PROCESSING")
                .scanDate(LocalDateTime.now())
                .build();

        try {
            Map<String, String> body = Map.of(
                    "image_url", imageUrl != null ? imageUrl : ""
            );

            String raw = webClient.post()
                    .uri(ANALYZE_URL)
                    .header("Content-Type", "application/json")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode node = objectMapper.readTree(raw);

            scan.setStatus("COMPLETED");
            scan.setBreedDetected(getField(node, "breed", "breed_detected", "breedDetected"));
            scan.setHasIssue(getBoolField(node, "has_issue", "hasIssue"));
            scan.setIssueName(getField(node, "issue_name", "issueName", "issue"));
            scan.setConfidence(getDoubleField(node, "confidence"));
            scan.setTreatmentTip(getField(node, "treatment_tip", "treatmentTip", "treatment"));

        } catch (Exception e) {
            scan.setStatus("FAILED");
            scan.setTreatmentTip("Analysis failed: " + e.getMessage());
        }

        return aiScanRepository.save(scan);
    }

    public Long getTotalScansCount() {
        return aiScanRepository.count();
    }

    private String getField(JsonNode node, String... keys) {
        for (String key : keys) {
            if (node.has(key) && !node.get(key).isNull())
                return node.get(key).asText();
        }
        return null;
    }

    private boolean getBoolField(JsonNode node, String... keys) {
        for (String key : keys) {
            if (node.has(key)) return node.get(key).asBoolean();
        }
        return false;
    }

    private double getDoubleField(JsonNode node, String... keys) {
        for (String key : keys) {
            if (node.has(key)) return node.get(key).asDouble();
        }
        return 0.0;
    }
}