package com.PAWPAW.pawpaw.message.controller;

import com.PAWPAW.pawpaw.message.dto.*;
import com.PAWPAW.pawpaw.message.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationResponse>> getConversations() {
        return ResponseEntity.ok(messageService.getMyConversations());
    }

    @GetMapping("/messages/{conversationId}")
    public ResponseEntity<List<MessageResponse>> getMessages(@PathVariable Long conversationId) {
        return ResponseEntity.ok(messageService.getMessages(conversationId));
    }

    @PostMapping("/messages")
    public ResponseEntity<MessageResponse> sendMessage(@RequestBody MessageRequest request) {
        return ResponseEntity.ok(messageService.sendMessage(request));
    }

    @DeleteMapping("/messages/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
        messageService.deleteMessage(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/messages/read/{conversationId}")
    public ResponseEntity<Void> markAsRead(@PathVariable Long conversationId) {
        messageService.getMessages(conversationId);
        return ResponseEntity.noContent().build();
    }
}