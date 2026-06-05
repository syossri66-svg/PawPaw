package com.PAWPAW.pawpaw.ai.controller;

import com.PAWPAW.pawpaw.ai.entity.AiScan;
import com.PAWPAW.pawpaw.ai.service.AiService;
import com.PAWPAW.pawpaw.auth.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    // 1. Endpoint لرفع الصور محلياً على السيرفر (لو الفرونت بيحتاجها منفصلة)
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
        }
        try {
            String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            String rootDir = System.getProperty("user.dir");
            File uploadDir = new File(rootDir + File.separator + "uploads");
            if (!uploadDir.exists()) uploadDir.mkdirs();
            File targetFile = new File(uploadDir + File.separator + filename);
            file.transferTo(targetFile);
            String fileUrl = "https://pawpaw-app.up.railway.app/uploads/" + filename;
            return ResponseEntity.ok(Map.of("imageUrl", fileUrl));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to store file: " + e.getMessage()));
        }
    }

    // 2. Endpoint التحليل الفوري للصورة ومخاطبة موديل غانم الجديد
    @PostMapping(value = "/visual-scan", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AiScan> processVisualScan(
            @RequestParam(value = "petId", required = false) Long petId,
            @RequestParam(value = "file") MultipartFile file) {

        Long userId = null;
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // لقط الـ User ID تلقائياً من الـ Token عشان نسيف العملية باسم المستخدم
        if (principal instanceof User) {
            userId = ((User) principal).getId();
        }


        AiScan scanResult = aiService.saveAndProcessVisualScan(petId, file, null, userId);
        return ResponseEntity.ok(scanResult);
    }


    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {

        Long totalScans = aiService.getTotalScansCount();

        return ResponseEntity.ok(Map.of(
                "accuracy", "94%",
                "breeds", "200+",
                "totalScans", totalScans
        ));
    }
}