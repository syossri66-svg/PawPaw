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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;


    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        System.out.println("Received file to upload: " + file.getOriginalFilename());

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
        }

        try {

            String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();


            File uploadDir = new File("uploads");
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }


            Path targetPath = Paths.get("uploads").resolve(filename);
            file.transferTo(targetPath.toFile());


            String fileUrl = "https://pawpaw-app.up.railway.app/uploads/" + filename;
            return ResponseEntity.ok(Map.of("imageUrl", fileUrl));

        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to store file: " + e.getMessage()));
        }
    }


    @PostMapping("/visual-scan")
    public ResponseEntity<AiScan> processVisualScan(
            @RequestParam("petId") Long petId,
            @RequestParam(value = "imageUrl", required = false) String imageUrl) {

        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = currentUser.getId();


        AiScan scanResult = aiService.saveAndProcessVisualScan(petId, null, imageUrl, userId).block();


        return ResponseEntity.ok(scanResult);
    }
}