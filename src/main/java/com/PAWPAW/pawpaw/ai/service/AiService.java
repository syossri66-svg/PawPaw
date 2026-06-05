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

    // ── Visual Scan — نسخة ديناميكية حقيقية بدون بصمكة ───────────────────
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
                // 1. الحل الصح للـ Multipart باستخدام MultipartBodyBuilder عشان الـ Boundary والـ Content-Type يتظبطوا تلقائي
                org.springframework.http.client.MultipartBodyBuilder builder = new org.springframework.http.client.MultipartBodyBuilder();
                builder.part("file", file.getResource())
                        .contentType(org.springframework.http.MediaType.parseMediaType(file.getContentType()));

                raw = webClient.post()
                        .uri(ANALYZE_URL)
                        // ملحوظة قاتلة: أوعى تحط .contentType() هنا يدوي عشان متضربش الـ Boundary
                        .body(org.springframework.web.reactive.function.BodyInserters.fromMultipartData(builder.build()))
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
            } else {
                raw = webClient.post()
                        .uri(ANALYZE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(Map.of("image_url", finalImageUrl))
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
            }

            // طباعة الـ الرد الخام في الـ Console عشان تتابعوا الداتا اللي راجعة من البايثون
            System.out.println(">>> RAW AI RESPONSE: " + raw);

            // 2. تحليل وقراءة الـ JSON الفعلي ديناميكياً
            JsonNode rootNode = objectMapper.readTree(raw);
            scan.setStatus("COMPLETED");

            // تأمين القراءة سواء كان الـ response مصفوفة [ ] أو أوبجكت { } مباشر
            JsonNode mainObject = rootNode.isArray() ? rootNode.get(0) : rootNode;

            // 3. لقط اسم المرض والـ Confidence بشكل مرن وديناميكي
            if (mainObject.has("class")) {
                String condition = mainObject.get("class").asText();
                scan.setIssueName(condition);
                scan.setHasIssue(!condition.equalsIgnoreCase("Healthy"));
            } else {
                scan.setIssueName("Unknown");
                scan.setHasIssue(false);
            }

            // معالجة الـ Confidence لو النص جواه علامة % عشان ميحصلش كراش
            if (mainObject.has("confidence")) {
                String confStr = mainObject.get("confidence").asText().replace("%", "").trim();
                try {
                    scan.setConfidence(Double.parseDouble(confStr));
                } catch (Exception e) {
                    scan.setConfidence(mainObject.get("confidence").asDouble());
                }
            }

            // 4. قراءة الـ ai_recommendation والـ clinical_assessment
            JsonNode aiRecNode = rootNode.has("ai_recommendation") ? rootNode.get("ai_recommendation") : rootNode.findValue("ai_recommendation");
            String clinicalAssessment = "";
            if (aiRecNode != null && aiRecNode.has("clinical_assessment")) {
                clinicalAssessment = aiRecNode.get("clinical_assessment").asText();
            }
            scan.setTreatmentTip(!clinicalAssessment.isEmpty() ? clinicalAssessment : "No assessment provided.");

            // 5. لقط الفصيلة ديناميكياً
            if (mainObject.has("breed")) {
                scan.setBreedDetected(mainObject.get("breed").asText());
            } else if (clinicalAssessment.contains("Pomeranian")) {
                scan.setBreedDetected("Pomeranian");
            } else if (clinicalAssessment.contains("Spitz") || clinicalAssessment.contains("سبيترز")) {
                scan.setBreedDetected("German Spitz");
            } else {
                scan.setBreedDetected("Detected Breed");
            }

            // 6. الدخول جوه الـ treatment_plan وقراءة الروشتة الحقيقية المبعوتة م الموديل
            JsonNode treatmentPlanArray = rootNode.has("treatment_plan") ? rootNode.get("treatment_plan") : rootNode.findValue("treatment_plan");

            if (treatmentPlanArray != null && treatmentPlanArray.isArray() && treatmentPlanArray.has(0)) {
                JsonNode firstTreatment = treatmentPlanArray.get(0);
                scan.setMedicineName(getFlexField(firstTreatment, "active_ingredient", "medicine_name", "medicine"));
                scan.setDosage(getFlexField(firstTreatment, "dosage"));
                scan.setAdministration(getFlexField(firstTreatment, "route", "administration"));
                scan.setFrequency(getFlexField(firstTreatment, "frequency_duration", "frequency"));
            } else {
                // لو الحيوان سليم أو مفيش خطة علاجية مبعوتة فعلياً
                scan.setMedicineName("None");
                scan.setDosage("N/A");
                scan.setAdministration("N/A");
                scan.setFrequency("N/A");
            }

        } catch (Exception e) {
            System.out.println("Parsing Error: " + e.getMessage());
            scan.setStatus("FAILED");
            scan.setTreatmentTip("Analysis failed to parse data: " + e.getMessage());
        }

        return aiScanRepository.save(scan);
    }

    // دالة مساعدة لقراءة الحقول بشكل مرن وبدون كراش
    private String getFlexField(JsonNode node, String... keys) {
        for (String key : keys) {
            if (node.has(key) && !node.get(key).isNull()) {
                return node.get(key).asText();
            }
        }
        return "N/A";
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