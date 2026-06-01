package com.PAWPAW.pawpaw.ai.controller;

import com.PAWPAW.pawpaw.ai.entity.AiScan;
import com.PAWPAW.pawpaw.ai.service.AiService;
import com.PAWPAW.pawpaw.auth.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    /**
     * 1️⃣ ميثود الـ Upload الحقيقية:
     * بتاخد الصورة، بتسيفها في فولدر uploads، وترجع لينك حقيقي ديناميك.
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<Map<String, String>> uploadFile(@RequestPart("file") FilePart file) {
        System.out.println("Received file to upload: " + file.filename());


        String filename = System.currentTimeMillis() + "_" + file.filename();


        Path targetPath = Paths.get("uploads").resolve(filename);


        File uploadDir = new File("uploads");
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // حفظ الملف على السيرفر بجد، ثم إرجاع اللينك الحقيقي لبوستمان أو الفرونت إند
        return file.transferTo(targetPath)
                .then(Mono.fromCallable(() -> {
                    String fileUrl = "https://pawpaw-app.up.railway.app/uploads/" + filename;
                    return Map.of("imageUrl", fileUrl);
                }));
    }


    @PostMapping("/visual-scan")
    public Mono<AiScan> processVisualScan(
            @RequestParam("petId") Long petId,
            @RequestParam(value = "imageUrl", required = false) String imageUrl) {


        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = currentUser.getId();


        return aiService.saveAndProcessVisualScan(petId, Mono.empty(), imageUrl, userId);
    }
}