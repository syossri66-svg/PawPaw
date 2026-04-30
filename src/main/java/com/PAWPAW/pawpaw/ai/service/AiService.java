package com.PAWPAW.pawpaw.ai.service;

import com.PAWPAW.pawpaw.ai.entity.AiMessage;
import com.PAWPAW.pawpaw.ai.repository.AiMessageRepository;
import com.PAWPAW.pawpaw.auth.entity.User; // تأكدي من مسار الـ User عندك
import org.springframework.ai.chat.ChatClient;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class AiService {

    private final ChatClient chatClient;
    private final AiMessageRepository aiRepository;

    // لازم الـ Constructor يشمل الـ الاثنين مع بعض
    public AiService(ChatClient chatClient, AiMessageRepository aiRepository) {
        this.chatClient = chatClient;
        this.aiRepository = aiRepository;
    }

    public String getChatResponse(String message, User user) {
        String response = chatClient.call(message);


        AiMessage aiMessage = AiMessage.builder()
                .userPrompt(message)
                .aiResponse(response)
                .createdAt(LocalDateTime.now())
                .user(user)
                .build();

        aiRepository.save(aiMessage);

        return response;
    }

    public String getSymptomAnalysis(String symptoms) {
        return chatClient.call("You are a professional veterinarian. Analyze: " + symptoms);
    }

    public String getMedicalReport(String data) {
        return chatClient.call("Generate a professional medical summary report for: " + data);
    }
}