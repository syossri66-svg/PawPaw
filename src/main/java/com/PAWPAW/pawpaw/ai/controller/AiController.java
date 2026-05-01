package com.PAWPAW.pawpaw.ai.controller;

import com.PAWPAW.pawpaw.ai.service.AiService;

import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/predict")
    public Mono<String> predict(@RequestBody Map<String, String> request) {
        return aiService.getAiResponse(request.get("description"));
    }
}