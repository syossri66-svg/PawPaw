package com.PAWPAW.pawpaw.ai.controller;

import com.PAWPAW.pawpaw.ai.service.AiChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/ai/chats")
@RequiredArgsConstructor
// ❌ شيلنا الـ CrossOrigin منعاً لتضارب الـ CORS والـ 403 على Railway
public class AiChatController {

    private final AiChatService aiChatService;

    // إنشاء شات جديد
    @PostMapping
    public ResponseEntity<?> createChat() {
        return ResponseEntity.ok(aiChatService.createChat());
    }

    // جيب كل الشاتات
    @GetMapping
    public ResponseEntity<?> getMyChats() {
        return ResponseEntity.ok(aiChatService.getMyChats());
    }

    // بعت رسالة نصية في شات معين
    @PostMapping("/{chatId}/messages")
    public ResponseEntity<?> sendMessage(
            @PathVariable Long chatId,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(aiChatService.sendMessage(chatId, body.get("message")));
    }

    // ✅ جديد: بعت رسالة مع صورة → يعمل visual scan تلقائي
    @PostMapping(value = "/{chatId}/scan-message", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> sendScanMessage(
            @PathVariable Long chatId,
            @RequestParam(value = "message", required = false) String message,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "imageUrl", required = false) String imageUrl) {
        return ResponseEntity.ok(
                aiChatService.sendMessageWithScan(chatId, message, file, imageUrl)
        );
    }

    // جيب رسائل شات معين
    @GetMapping("/{chatId}/messages")
    public ResponseEntity<?> getChatMessages(@PathVariable Long chatId) {
        return ResponseEntity.ok(aiChatService.getChatMessages(chatId));
    }

    // Pin
    @PatchMapping("/{chatId}/pin")
    public ResponseEntity<?> pinChat(@PathVariable Long chatId) {
        return ResponseEntity.ok(aiChatService.updateStatus(chatId, "PINNED"));
    }

    // Archive
    @PatchMapping("/{chatId}/archive")
    public ResponseEntity<?> archiveChat(@PathVariable Long chatId) {
        return ResponseEntity.ok(aiChatService.updateStatus(chatId, "ARCHIVED"));
    }

    // Delete
    @DeleteMapping("/{chatId}")
    public ResponseEntity<Void> deleteChat(@PathVariable Long chatId) {
        aiChatService.deleteChat(chatId);
        return ResponseEntity.noContent().build();
    }
}