package com.PAWPAW.pawpaw.ai.service;

import com.PAWPAW.pawpaw.ai.entity.AiMessage;
import com.PAWPAW.pawpaw.ai.repository.AiMessageRepository;
import com.PAWPAW.pawpaw.auth.entity.User;
import org.springframework.ai.chat.ChatClient;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AiService {

    private final ChatClient chatClient;
    private final AiMessageRepository aiRepository;


    public AiService(ChatClient chatClient, AiMessageRepository aiRepository) {
        this.chatClient = chatClient;
        this.aiRepository = aiRepository;
    }

    public String getChatResponse(String message, User user) {
        try {
            String response = chatClient.call(message);

            return response;
        } catch (Exception e) {

            System.err.println("AI Error: " + e.getMessage());
            return "Error calling AI: " + e.getMessage();
        }
    }
    public List<AiMessage> getUserChatHistory(UUID userId) {

        return aiRepository.findMessagesByUserId(userId);
    }

    public String getSymptomAnalysis(String symptoms) {
        return chatClient.call("You are a professional veterinarian. Analyze: " + symptoms);
    }

    public String getMedicalReport(String data) {
        return chatClient.call("Generate a professional medical summary report for: " + data);
    }
}