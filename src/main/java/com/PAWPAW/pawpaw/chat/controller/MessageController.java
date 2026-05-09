package com.PAWPAW.pawpaw.chat.controller;

import com.PAWPAW.pawpaw.chat.dto.ConversationResponse;
import com.PAWPAW.pawpaw.chat.dto.MessageRequest;
import com.PAWPAW.pawpaw.chat.dto.MessageResponse;
import com.PAWPAW.pawpaw.chat.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<MessageResponse> sendMessage(@Valid @RequestBody MessageRequest request) {
        return ResponseEntity.ok(messageService.sendMessage(request));
    }

    @GetMapping("/{conversationId}")
    public ResponseEntity<List<MessageResponse>> getConversation(@PathVariable Long otherUserId) {
        return ResponseEntity.ok(messageService.getConversation(otherUserId));
    }

    @GetMapping("/api/conversations")
    public ResponseEntity<List<ConversationResponse>> getMyConversations() {
        return ResponseEntity.ok(messageService.getMyConversations());
    }

    @GetMapping("/unread/{senderId}")
    public ResponseEntity<Long> getUnreadCount(@PathVariable Long senderId) {
        return ResponseEntity.ok(messageService.getUnreadCount(senderId));
    }
    // مسح رسالة معينة
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
        messageService.deleteMessage(id); // هتحتاجي تضيفي الميثود دي في الـ Service
        return ResponseEntity.noContent().build();
    }

    // تغيير حالة المحادثة كلها لـ "Seen"
    @PatchMapping("/read/{conversationId}")
    public ResponseEntity<Void> markAsSeen(@PathVariable Long conversationId) {
        messageService.markConversationAsRead(conversationId); // هتحتاجي تضيفيها في الـ Service
        return ResponseEntity.ok().build();
    }
}