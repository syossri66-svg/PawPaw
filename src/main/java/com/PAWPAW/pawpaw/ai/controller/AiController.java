package com.PAWPAW.pawpaw.ai.controller;

import com.PAWPAW.pawpaw.ai.service.AiService;
import com.PAWPAW.pawpaw.auth.entity.User;
import com.PAWPAW.pawpaw.auth.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(AiController.class);


    public AiController(AiService aiService, UserRepository userRepository) {
        this.aiService = aiService;
        this.userRepository = userRepository;
    }

    @PostMapping("/chat")
    public String chat(@RequestBody String message, Authentication authentication) {

        String email = authentication.getName();


        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));


        return aiService.getChatResponse(message, user);
    }

    @PostMapping("/predict")
    public String predict(@RequestBody String symptoms) {
        return aiService.getSymptomAnalysis(symptoms);
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