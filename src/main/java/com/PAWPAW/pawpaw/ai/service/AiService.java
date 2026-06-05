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
    private static final String ANALYZE_URL = "https://maghanem-pawpaw-owner-api.hf.space/analyze"; // اللينك الجديد المحدث هنا 🚀

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

    // ── Visual Scan — بيبعت الصورة كـ multipart/form-data ───────────────────
    public AiScan saveAndProcessVisualScan(Long petId, MultipartFile file, String imageUrl, Long userId) {

        String finalImageUrl = (imageUrl != null && !imageUrl.isEmpty()) ? imageUrl : "";
        if (file != null && !file.isEmpty() && finalImageUrl.isEmpty()) {
            finalImageUrl = "https://pawpaw-app.up.railway.app/uploads/" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
        }

        // تجهيز الأوبجكت المبدئي
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

            JsonNode node = objectMapper.readTree(raw);
            scan.setStatus("COMPLETED");

            // 1. قراءة البيانات الأساسية
            scan.setBreedDetected(getField(node, "breed", "breed_detected", "breedDetected"));
            boolean hasIssue = getBoolField(node, "has_issue", "hasIssue");
            String label = getField(node, "label", "issue_name", "issueName", "issue");
            scan.setConfidence(getDoubleField(node, "score", "confidence"));

            // 2. قراءة تفاصيل خطة العلاج (الـ Recommendation الـ Dynamic من الـ AI)
            if ("Healthy".equalsIgnoreCase(label) || "Normal".equalsIgnoreCase(label) || !hasIssue) {
                scan.setHasIssue(false);
                scan.setIssueName("Healthy");
                scan.setTreatmentTip("Your pet looks perfectly healthy! No medical treatment needed.");

                // داتا ديفولت لو الحيوان سليم
                scan.setMedicineName("None");
                scan.setDosage("N/A");
                scan.setAdministration("N/A");
                scan.setFrequency("0 times per week");
            } else {
                scan.setHasIssue(true);
                scan.setIssueName(label != null ? label : "Detected Issue");
                scan.setTreatmentTip(getField(node, "treatment_tip", "treatmentTip", "treatment"));

                // 🌟 لقط تفاصيل خطة العلاج والجرعات ديناميكياً من الـ JSON اللي جاي من غانم
                scan.setMedicineName(getField(node, "medicine_name", "medicine", "medicineName"));
                scan.setDosage(getField(node, "dosage", "dose"));
                scan.setAdministration(getField(node, "administration", "how_to_take", "method"));
                scan.setFrequency(getField(node, "frequency", "times_per_week"));

                // خطة احتياطية (Fallback) لو غانم مبعتش الفيلدز دي متفصصة عشان الكود ميضربش null
                if (scan.getMedicineName() == null) scan.setMedicineName("Antibiotic Ointment / Supplement");
                if (scan.getDosage() == null) scan.setDosage("Apply a small amount / 1 pill");
                if (scan.getAdministration() == null) scan.setAdministration("Topical use / Orally");
                if (scan.getFrequency() == null) scan.setFrequency("Once daily (7 times a week)");
            }

        } catch (Exception e) {
            System.out.println("Scan Process Error: " + e.getMessage());
            scan.setStatus("FAILED");
            scan.setTreatmentTip("Analysis failed: " + e.getMessage());
        }

        return aiScanRepository.save(scan);
    }

    // 4. ميثود الـ Stats عشان تحسب إجمالي الاسكانز الحقيقية من الجدول علطول
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