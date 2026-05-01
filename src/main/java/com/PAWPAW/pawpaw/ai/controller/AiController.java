package com.PAWPAW.pawpaw.ai.controller;

import com.PAWPAW.pawpaw.ai.service.AiService;
import com.PAWPAW.pawpaw.auth.entity.User;
import com.PAWPAW.pawpaw.auth.repository.UserRepository;
import org.springframework.ai.chat.model.ChatModel;


import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(AiController.class);

    // استخدام ChatModel العام لضمان التوافق
    private final ChatModel chatModel;

    public AiController(AiService aiService, UserRepository userRepository, ChatModel chatModel) {
        this.aiService = aiService;
        this.userRepository = userRepository;
        this.chatModel = chatModel;
    }

    @PostMapping("/chat")
    public String chat(@RequestBody String message, Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return aiService.getChatResponse(message, user);
    }

    @PostMapping("/predict")
    public ResponseEntity<String> predict(@RequestBody Map<String, String> request) {
        try {
            String message = request.get("description");

            System.out.println("Message received: " + message);

            String response = chatModel.call(message);
            return ResponseEntity.ok(response);
        } catch (Exception e) {

            return ResponseEntity.status(500).body("Error details: " + e.getMessage());
        }
    }

    @PostMapping("/report")
    public String generateReport(@RequestBody String data) {
        return aiService.getMedicalReport(data);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAiException(Exception e) {
        logger.error("AI Service Error: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("The AI service is currently busy. Please try again later.");
    }
}