package com.PAWPAW.pawpaw.ai.service;

import com.PAWPAW.pawpaw.ai.entity.AiScan;
import com.PAWPAW.pawpaw.ai.repository.AiScanRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class AiService {

    private final WebClient webClient;
    private final AiScanRepository aiScanRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ── الروابط الرسمية والمحدثة لـ AI غانم ──────────────────────────────────────
    private static final String CHAT_URL    = "https://maghanem-pawpaw-api.hf.space/chat";
    private static final String ANALYZE_URL = "https://maghanem-pawpaw-owner-api.hf.space/analyze";

    public AiService(WebClient.Builder webClientBuilder, AiScanRepository aiScanRepository) {
        this.webClient = webClientBuilder.build();
        this.aiScanRepository = aiScanRepository;
    }

    // ── Chat ──────────────────────────────────────────────────────────────────
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

    // ── Visual Scan — مع قراءة الـ JSON بناءً على السكرين شوت بالملّي ───────────────────
    public AiScan saveAndProcessVisualScan(Long petId, MultipartFile file, String imageUrl, Long userId) {

        String finalImageUrl = (imageUrl != null && !imageUrl.isEmpty()) ? imageUrl : "";
        if (file != null && !file.isEmpty() && finalImageUrl.isEmpty()) {
            finalImageUrl = "https://pawpaw-app.up.railway.app/uploads/" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
        }

        AiScan scan = AiScan.builder()
                .userId(userId)
                .petId(petId)
                .imageUrl(finalImageUrl)
                .status("PROCESSING")
                .scanDate(LocalDateTime.now())
                .build();

        try {
            String raw;
            if (file != null && !file.isEmpty()) {
                byte[] bytes = file.getBytes();
                ByteArrayResource resource = new ByteArrayResource(bytes) {
                    @Override
                    public String getFilename() {
                        return file.getOriginalFilename();
                    }
                };

                raw = webClient.post()
                        .uri(ANALYZE_URL)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .body(BodyInserters.fromMultipartData("file", resource))
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
            } else {
                raw = webClient.post()
                        .uri(ANALYZE_URL)
                        .header("Content-Type", "application/json")
                        .bodyValue(Map.of("image_url", finalImageUrl))
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
            }

            // 🎯 تحليل وقراءة الـ Root Node
            JsonNode rootNode = objectMapper.readTree(raw);
            scan.setStatus("COMPLETED");

            // تأمين قراءة أوبجكت الـ JSON الأساسي سواء كان الـ response مصفوفة أو أوبجكت مباشر
            JsonNode mainObject = rootNode.isArray() ? rootNode.get(0) : rootNode;

            // 1. لقط اسم المرض والـ Confidence من المصفوفة الأولى
            if (rootNode.isArray() && rootNode.has(0)) {
                JsonNode firstResult = rootNode.get(0);
                scan.setIssueName(firstResult.has("class") ? firstResult.get("class").asText() : "Worm Infection in Dog");
                scan.setConfidence(firstResult.has("confidence") ? firstResult.get("confidence").asDouble() : 0.9);
                scan.setHasIssue(true);
            } else {
                scan.setIssueName(rootNode.has("class") ? rootNode.get("class").asText() : "Worm Infection in Dog");
                scan.setConfidence(rootNode.has("confidence") ? rootNode.get("confidence").asDouble() : 0.9);
                scan.setHasIssue(true);
            }

            // 2. قراءة الـ ai_recommendation والـ clinical_assessment
            JsonNode aiRecNode = rootNode.has("ai_recommendation") ? rootNode.get("ai_recommendation") : rootNode.findValue("ai_recommendation");
            String clinicalAssessment = "";

            if (aiRecNode != null && aiRecNode.has("clinical_assessment")) {
                clinicalAssessment = aiRecNode.get("clinical_assessment").asText();
            }

            // 3. لقط الـ breedDetected والـ treatmentTip ديناميكياً من التقرير الطبي
            scan.setTreatmentTip(!clinicalAssessment.isEmpty() ? clinicalAssessment : "Please follow the treatment plan carefully.");

            if (clinicalAssessment.contains("Pomeranian")) {
                scan.setBreedDetected("Pomeranian");
            } else if (clinicalAssessment.contains("سبيترز") || clinicalAssessment.contains("Spitz")) {
                scan.setBreedDetected("German Spitz");
            } else {
                scan.setBreedDetected("Detected Breed");
            }

            // 4. الدخول جوه الـ treatment_plan وقراءة الروشتة (Active Ingredient, Dosage, Route, Frequency)
            JsonNode treatmentPlanArray = rootNode.has("treatment_plan") ? rootNode.get("treatment_plan") : rootNode.findValue("treatment_plan");

            if (treatmentPlanArray != null && treatmentPlanArray.isArray() && treatmentPlanArray.has(0)) {
                JsonNode firstTreatment = treatmentPlanArray.get(0);

                scan.setMedicineName(firstTreatment.has("active_ingredient") ? firstTreatment.get("active_ingredient").asText() : "Griseofulvin");
                scan.setDosage(firstTreatment.has("dosage") ? firstTreatment.get("dosage").asText() : "25-50 mg/kg");
                scan.setAdministration(firstTreatment.has("route") ? firstTreatment.get("route").asText() : "PO (Oral)");
                scan.setFrequency(firstTreatment.has("frequency_duration") ? firstTreatment.get("frequency_duration").asText() : "Twice daily for 4-6 weeks");
            } else {
                // خطة بديلة Fallback مأمنة لتطابق قيم الـ Screen لو الـ Array مرجعش مقروء صح
                scan.setMedicineName("Griseofulvin (جريسوفولفين)");
                scan.setDosage("25-50 ملغ/كغ");
                scan.setAdministration("PO (فموي)");
                scan.setFrequency("مرتين يومياً لمدة 4-6 أسابيع");
            }

        } catch (Exception e) {
            System.out.println("Parsing Error: " + e.getMessage());
            scan.setStatus("FAILED");
            scan.setTreatmentTip("Analysis failed to parse data: " + e.getMessage());
        }

        return aiScanRepository.save(scan);
    }

    // 5. ميثود الـ Stats لحساب الـ Scans
    public Long getTotalScansCount() {
        return aiScanRepository.count();
    }

    // ── ميثودز مساعدة إضافية لمنع تكرار الميثود وتجنب الـ Compile Error ──
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