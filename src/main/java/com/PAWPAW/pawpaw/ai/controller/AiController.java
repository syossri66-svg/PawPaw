package com.PAWPAW.pawpaw.ai.controller;

import com.PAWPAW.pawpaw.ai.service.AiService;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/predict")
    public Mono<String> predict(@RequestBody Map<String, String> request) {
        return aiService.getAiResponse(request.get("description"));
    }


    @PostMapping(value = "/visual-scan", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<Map<String, Object>> processVisualScan(
            @RequestParam(value = "petId", required = false) Long petId,
            @RequestPart(value = "image", required = false) Mono<FilePart> filePartMono,
            @RequestParam(value = "imageUrl", required = false) String imageUrl) {

        Long currentUserId = 1L;

        return aiService.saveAndProcessVisualScan(petId, filePartMono, imageUrl, currentUserId)
                .map(scan -> Map.of(
                        "scanId", scan.getId(),
                        "status", scan.getStatus(),
                        "result", Map.of(
                                "breedDetected", scan.getBreedDetected(),
                                "isHealthy", !scan.isHasIssue(),
                                "detectedIssue", scan.getIssueName(),
                                "confidenceScore", scan.getConfidence() + "%",
                                "recommendation", scan.getTreatmentTip()
                        ),
                        "scanDate", scan.getScanDate().toString()
                ));
    }


    @GetMapping("/stats")
    public Mono<Map<String, Object>> getAiStats() {
        return aiService.getTotalScansCount()
                .map(count -> Map.of(
                        "accuracy", "94%",
                        "breeds", "200+",
                        "totalScans", count
                ));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<Map<String, String>> uploadFile(@RequestPart("file") Mono<FilePart> filePartMono) {

        String mockImageUrl = "https://pawpaw-bucket.s3.amazonaws.com/uploaded-pet-image.jpg";

        return Mono.just(Map.of("imageUrl", mockImageUrl));
    }
}