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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AiService {

    private final WebClient webClient;
    private final AiScanRepository aiScanRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ✅ URLs المصححة - owner-api
    private static final String CHAT_URL    = "https://maghanem-pawpaw-owner-api.hf.space/chat";
    private static final String ANALYZE_URL = "https://maghanem-pawpaw-owner-api.hf.space/analyze";

    public AiService(WebClient.Builder webClientBuilder, AiScanRepository aiScanRepository) {
        HttpClient httpClient = HttpClient.create().followRedirect(true);
        this.webClient = webClientBuilder
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
        this.aiScanRepository = aiScanRepository;
    }

    // ── Chat ─────────────────────────────────────────────────────────────────
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
                        if (node.has("data") && node.get("data").has("reply")) {
                            return node.get("data").get("reply").asText();
                        }
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

    // ── Analyze (Visual Scan) ─────────────────────────────────────────────────
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

            // ── Breed ──────────────────────────────────────────────────────
            if (dataNode.has("breed_predictions") && dataNode.get("breed_predictions").isArray()
                    && dataNode.get("breed_predictions").size() > 0) {
                JsonNode firstBreed = dataNode.get("breed_predictions").get(0);
                scan.setBreedDetected(getField(firstBreed, "class"));
            } else {
                scan.setBreedDetected("N/A");
            }

            // ── Disease ────────────────────────────────────────────────────
            if (dataNode.has("disease_predictions") && dataNode.get("disease_predictions").isArray()
                    && dataNode.get("disease_predictions").size() > 0) {
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

            // ── AI Recommendation + Treatment ─────────────────────────────
            if (dataNode.has("ai_recommendation")) {
                JsonNode aiRec = dataNode.get("ai_recommendation");

                if (aiRec.has("treatment_plan") && aiRec.get("treatment_plan").isArray()
                        && aiRec.get("treatment_plan").size() > 0) {
                    JsonNode treatment = aiRec.get("treatment_plan").get(0);
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

                // ✅ بنبني الـ Summary من كل fields الـ ai_recommendation
                scan.setSummary(buildSummary(scan, aiRec));

            } else {
                scan.setTreatmentTip("AI Recommendation details are not available.");
                scan.setSummary(buildBasicSummary(scan));
            }

        } catch (Exception e) {
            System.out.println(">>> AI PARSING ERROR: " + e.getMessage());
            scan.setStatus("FAILED");
            scan.setTreatmentTip("Analysis failed to parse: " + e.getMessage());
            scan.setSummary("Analysis could not be completed. Please try again.");
        }

        return aiScanRepository.save(scan);
    }

    // ── Pet Report Analysis ───────────────────────────────────────────────────
    public String analyzePetData(String petName, String breed, String age, String weight) {
        String prompt = String.format(
                "You are a veterinary AI assistant. Analyze this pet's data and give a health summary.\n" +
                        "Pet Name: %s\nBreed: %s\nAge: %s\nWeight: %s\n\n" +
                        "If the pet seems healthy, say so clearly. " +
                        "If there is a potential issue based on weight or breed, mention it and suggest a solution. " +
                        "Keep the response under 3 sentences.",
                petName, breed, age, weight
        );
        try {
            return getAiResponse(prompt).block();
        } catch (Exception e) {
            return petName + " appears to be in good condition. AI analysis is temporarily unavailable.";
        }
    }

    public Long getTotalScansCount() {
        return aiScanRepository.count();
    }

    // ── Summary Builder ───────────────────────────────────────────────────────
    /**
     * بيبني summary كامل من كل fields الـ ai_recommendation
     */
    private String buildSummary(AiScan scan, JsonNode aiRec) {
        StringBuilder sb = new StringBuilder();

        // العنوان
        sb.append("🔍 Visual Scan Summary\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━\n\n");

        // Breed
        sb.append("🐾 Detected Breed: ").append(scan.getBreedDetected()).append("\n");

        // Condition
        if (scan.isHasIssue()) {
            sb.append("⚠️ Detected Condition: ").append(scan.getIssueName())
                    .append(String.format(" (%.1f%% confidence)\n", scan.getConfidence()));
        } else {
            sb.append("✅ No significant health issues detected.\n");
        }

        // Clinical Assessment
        String clinicalAssessment = getFieldSafe(aiRec, "clinical_assessment");
        if (!clinicalAssessment.equals("N/A")) {
            sb.append("\n📋 Clinical Assessment:\n").append(clinicalAssessment).append("\n");
        }

        // Severity
        String severity = getFieldSafe(aiRec, "case_severity");
        if (!severity.equals("N/A")) {
            sb.append("\n🔴 Case Severity: ").append(severity).append("\n");
        }

        // Urgent Referral
        if (aiRec.has("needs_urgent_referral") && aiRec.get("needs_urgent_referral").asBoolean()) {
            sb.append("🚨 Urgent referral is recommended!\n");
        }

        // Differential Diagnoses
        if (aiRec.has("differential_diagnoses") && aiRec.get("differential_diagnoses").isArray()
                && aiRec.get("differential_diagnoses").size() > 0) {
            sb.append("\n🧬 Possible Diagnoses:\n");
            aiRec.get("differential_diagnoses").forEach(d -> sb.append("  • ").append(d.asText()).append("\n"));
        }

        // Recommended Tests
        if (aiRec.has("recommended_tests") && aiRec.get("recommended_tests").isArray()
                && aiRec.get("recommended_tests").size() > 0) {
            sb.append("\n🧪 Recommended Tests:\n");
            aiRec.get("recommended_tests").forEach(t -> sb.append("  • ").append(t.asText()).append("\n"));
        }

        // Red Flags
        if (aiRec.has("red_flags") && aiRec.get("red_flags").isArray()
                && aiRec.get("red_flags").size() > 0) {
            sb.append("\n🚩 Red Flags:\n");
            aiRec.get("red_flags").forEach(f -> sb.append("  • ").append(f.asText()).append("\n"));
        }

        // Contraindications
        if (aiRec.has("contraindications") && aiRec.get("contraindications").isArray()
                && aiRec.get("contraindications").size() > 0) {
            sb.append("\n⛔ Contraindications:\n");
            aiRec.get("contraindications").forEach(c -> sb.append("  • ").append(c.asText()).append("\n"));
        }

        // Clinical Notes
        String clinicalNotes = getFieldSafe(aiRec, "clinical_notes");
        if (!clinicalNotes.equals("N/A")) {
            sb.append("\n📝 Clinical Notes:\n").append(clinicalNotes).append("\n");
        }

        // Confidence Comment
        String confidenceComment = getFieldSafe(aiRec, "confidence_comment");
        if (!confidenceComment.equals("N/A")) {
            sb.append("\nℹ️ Note: ").append(confidenceComment).append("\n");
        }

        return sb.toString();
    }

    /**
     * Fallback لو مفيش ai_recommendation
     */
    private String buildBasicSummary(AiScan scan) {
        if (scan.isHasIssue()) {
            return String.format(
                    "🔍 Scan completed for %s.\n⚠️ Detected: %s (%.1f%% confidence).\nPlease consult a veterinarian for further examination.",
                    scan.getBreedDetected(), scan.getIssueName(), scan.getConfidence()
            );
        }
        return String.format(
                "🔍 Scan completed for %s.\n✅ No significant health issues detected.\nYour pet appears to be in good health!",
                scan.getBreedDetected()
        );
    }

    private String getField(JsonNode node, String... keys) {
        for (String key : keys) {
            if (node.has(key) && !node.get(key).isNull())
                return node.get(key).asText();
        }
        return "N/A";
    }

    private String getFieldSafe(JsonNode node, String key) {
        if (node.has(key) && !node.get(key).isNull() && !node.get(key).asText().isBlank()) {
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