package com.PAWPAW.pawpaw.vet.controller;

import org.springframework.ai.chat.ChatClient;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final ChatClient chatClient;


    public AiController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @PostMapping("/predict")
    public String predict(@RequestBody String message) {

        return chatClient.call(message);
    }
}