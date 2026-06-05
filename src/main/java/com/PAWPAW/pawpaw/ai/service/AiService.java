package com.PAWPAW.pawpaw.ai.service;

import com.PAWPAW.pawpaw.ai.entity.AiScan;
import com.PAWPAW.pawpaw.ai.repository.AiScanRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class AiService {

    private final WebClient webClient;
    private final AiScanRepository aiScanRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String CHAT_URL    = "https://maghanem-pawpaw-api.hf.space/chat";
    private static final String ANALYZE_URL = "https://maghanem-pawpaw-api.hf.space/analyze/";

    public AiService(WebClient.Builder webClientBuilder, AiScanRepository aiScanRepository) {
        HttpClient httpClient = HttpClient.create().followRedirect(true);
        this.webClient = webClientBuilder
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
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
                        // ✅ جربي data.reply الأول
                        if (node.has("data") && node.get("data").has("reply")) {
                            return node.get("data").get("reply").asText();
                        }
                        // باقي المفاتيح fallback
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

    public AiScan saveAndProcessVisualScan(Long petId, MultipartFile file, String imageUrl, Long userId) {
        AiScan scan = AiScan.builder()
                .userId(userId)
                .petId(petId)
                .imageUrl(imageUrl != null ? imageUrl : "")
                .status("PROCESSING")
                .scanDate(LocalDateTime.now())
                .build();

        try {
            String raw;
            if (file != null && !file.isEmpty()) {
                MultipartBodyBuilder builder = new MultipartBodyBuilder();
                builder.part("file", file.getResource())
                        .contentType(MediaType.parseMediaType(file.getContentType()));

                raw = webClient.post()
                        .uri(ANALYZE_URL)
                        .body(BodyInserters.fromMultipartData(builder.build()))
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
            } else {
                raw = webClient.post()
                        .uri(ANALYZE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(Map.of("image_url", imageUrl != null ? imageUrl : ""))
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
            }

            System.out.println(">>> RAW HUGGINGFACE RESPONSE: " + raw);

            JsonNode rootNode = objectMapper.readTree(raw);


            JsonNode dataNode = rootNode.has("data") ? rootNode.get("data") : rootNode;

            scan.setStatus("COMPLETED");


            if (dataNode.has("breed_predictions") && dataNode.get("breed_predictions").isArray() && dataNode.get("breed_predictions").size() > 0) {
                JsonNode firstBreed = dataNode.get("breed_predictions").get(0);
                scan.setBreedDetected(getField(firstBreed, "class"));
            } else {
                scan.setBreedDetected("N/A");
            }


            if (dataNode.has("disease_predictions") && dataNode.get("disease_predictions").isArray() && dataNode.get("disease_predictions").size() > 0) {
                JsonNode firstDisease = dataNode.get("disease_predictions").get(0);
                String diseaseName = getField(firstDisease, "class");
                scan.setIssueName(diseaseName);
                scan.setConfidence(getDoubleField(firstDisease, "confidence"));


                scan.setHasIssue(!diseaseName.equalsIgnoreCase("Healthy"));
            } else {
                scan.setIssueName("N/A");
                scan.setConfidence(0.0);
                scan.setHasIssue(false);
            }


            if (dataNode.has("ai_recommendation")) {
                JsonNode aiRec = dataNode.get("ai_recommendation");

                if (aiRec.has("treatment_plan") && aiRec.get("treatment_plan").isArray() && aiRec.get("treatment_plan").size() > 0) {
                    JsonNode treatment = aiRec.get("treatment_plan").get(0);

                    // بنبني الـ Tip بشكل منسق يعتمد على الـ English Keys لتفادي مشاكل الـ Encoding العربي
                    String fullPrescription = String.format(
                            "Medical Analysis for %s:\n- Detected Condition: %s\n- Active Ingredient Needed: %s\n- Recommended Dosage: %s\n- Administration Route: %s\n- Frequency & Duration: %s",
                            scan.getBreedDetected(),
                            scan.getIssueName(),
                            getField(treatment, "active_ingredient"),
                            getField(treatment, "dosage"),
                            getField(treatment, "route"),
                            getField(treatment, "frequency_duration")
                    );
                    scan.setTreatmentTip(fullPrescription);
                } else {
                    scan.setTreatmentTip("No explicit treatment plan provided by the AI.");
                }
            } else {
                scan.setTreatmentTip("AI Recommendation details are not available.");
            }

        } catch (Exception e) {
            System.out.println(">>> AI PARSING ERROR: " + e.getMessage());
            scan.setStatus("FAILED");
            scan.setTreatmentTip("Analysis failed to parse: " + e.getMessage());
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
        return "N/A";
    }

    private double getDoubleField(JsonNode node, String... keys) {
        for (String key : keys) {
            if (node.has(key)) {
                try {
                    return Double.parseDouble(node.get(key).asText().replace("%", "").trim());
                } catch (Exception e) {
                    return node.get(key).asDouble();
                }
            }
        }
        return 0.0;
    }
}