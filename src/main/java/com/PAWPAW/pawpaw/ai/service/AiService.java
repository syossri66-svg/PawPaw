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

    // ✅ حل المشكلة الأولى: إضافة الـ Trailing Slash (/) لأن Hugging Face بيعمل Redirect إجباري من غيرها
    private static final String ANALYZE_URL = "https://maghanem-pawpaw-api.hf.space/analyze/";

    public AiService(WebClient.Builder webClientBuilder, AiScanRepository aiScanRepository) {
        // ✅ حل المشكلة الثانية: إجبار الـ WebClient على تتبع الـ Redirects (Follow Redirect) ليتوافق مع بروكسي Hugging Face
        HttpClient httpClient = HttpClient.create().followRedirect(true);

        this.webClient = webClientBuilder
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
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

    // ── Visual Scan ───────────────────────────────────────────────────────────
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
                // ✅ حل المشكلة الثالثة: استخدام MultipartBodyBuilder عشان الـ Boundary يتكون تلقائي وميحصلش رفض للملف من السيرفر
                MultipartBodyBuilder builder = new MultipartBodyBuilder();
                builder.part("file", file.getResource())
                        .contentType(MediaType.parseMediaType(file.getContentType()));

                raw = webClient.post()
                        .uri(ANALYZE_URL)
                        // 🔥 ممنوع نهائياً تحديد الـ .contentType() هنا يدوي عشان متضربش التشييد والـ Boundary الخاص بالملف
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

            // 🔍 بيطبع الرد الحقيقي اللي جاي من الـ AI في الـ Console للـ Debugging والتأكد
            System.out.println(">>> RAW HUGGINGFACE RESPONSE: " + raw);

            JsonNode node = objectMapper.readTree(raw);

            // ⚠️ التعديل العبقري: معالجة إذا كان الرد جاي في Array [ ] أو Object { } مباشرة
            JsonNode mainObject = node.isArray() ? node.get(0) : node;

            scan.setStatus("COMPLETED");

            // قراءة الحقول الأساسية بشكل مرن يدعم الـ Snake Case والـ Camel Case والـ class المتغيرة من غانم
            scan.setBreedDetected(getField(mainObject, "breed", "breed_detected", "breedDetected"));
            scan.setHasIssue(getBoolField(mainObject, "has_issue", "hasIssue"));
            scan.setIssueName(getField(mainObject, "issue_name", "issueName", "issue", "class"));
            scan.setConfidence(getDoubleField(mainObject, "confidence"));

            // 🔥 قفزة الثقة للداتا المعقدة:
            // 1. بندخل أولاً جوه أوبجكت الـ ai_recommendation لو موجود، لو مش موجود بنكمل على الـ mainObject الأساسي
            JsonNode recommendationNode = mainObject.has("ai_recommendation") ? mainObject.get("ai_recommendation") : mainObject;

            // 2. بنشوف لو جوه الـ recommendation فيه مصفوفة علاج treatment_plan [ ] بناخد أول عنصر فيها وبنقرا تفاصيل الروشتة الحية
            if (recommendationNode.has("treatment_plan") && recommendationNode.get("treatment_plan").isArray() && recommendationNode.get("treatment_plan").size() > 0) {
                JsonNode firstTreatment = recommendationNode.get("treatment_plan").get(0);

                String fullPrescription = String.format(
                        "Clinical Assessment: %s\n\nPrescription:\n- Active Ingredient: %s\n- Dosage: %s\n- Route: %s\n- Frequency & Duration: %s",
                        getField(recommendationNode, "clinical_assessment", "direct_answer"),
                        getField(firstTreatment, "active_ingredient"),
                        getField(firstTreatment, "dosage"),
                        getField(firstTreatment, "route"),
                        getField(firstTreatment, "frequency_duration")
                );
                scan.setTreatmentTip(fullPrescription);
            } else {
                // لو الرد مسطح وقديم من غير تفرعات بيقرأ بالطريقة الاحتياطية العادية علطول
                scan.setTreatmentTip(getField(recommendationNode, "treatment_tip", "treatmentTip", "treatment", "clinical_assessment", "direct_answer"));
            }

        } catch (Exception e) {
            System.out.println(">>> AI STACKTRACE ERROR: " + e.getMessage());
            scan.setStatus("FAILED");
            scan.setTreatmentTip("Analysis failed: " + e.getMessage());
        }

        return aiScanRepository.save(scan);
    }

    public Long getTotalScansCount() {
        return aiScanRepository.count();
    }

    // ── ميثودز المساعدة لقراءة الداتا بمرونة تامة وبدون كراشات ──────────────────
    private String getField(JsonNode node, String... keys) {
        for (String key : keys) {
            if (node.has(key) && !node.get(key).isNull())
                return node.get(key).asText();
        }
        return "N/A";
    }

    private boolean getBoolField(JsonNode node, String... keys) {
        for (String key : keys) {
            if (node.has(key)) return node.get(key).asBoolean();
        }
        return false;
    }

    private double getDoubleField(JsonNode node, String... keys) {
        for (String key : keys) {
            if (node.has(key)) {
                try {
                    // عشان لو الـ confidence رجع نص وفيه علامة % نعملها تنظيف ويتحول لـ double سليم
                    return Double.parseDouble(node.get(key).asText().replace("%", "").trim());
                } catch (Exception e) {
                    return node.get(key).asDouble();
                }
            }
        }
        return 0.0;
    }
}