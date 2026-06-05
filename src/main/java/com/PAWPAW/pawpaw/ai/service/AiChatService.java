package com.PAWPAW.pawpaw.ai.service;

import com.PAWPAW.pawpaw.ai.dto.AiChatResponse;
import com.PAWPAW.pawpaw.ai.entity.AiChat;
import com.PAWPAW.pawpaw.ai.entity.AiMessage;
import com.PAWPAW.pawpaw.ai.repository.AiChatRepository;
import com.PAWPAW.pawpaw.ai.repository.AiMessageRepository;
import com.PAWPAW.pawpaw.auth.entity.User;
import com.PAWPAW.pawpaw.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiChatService {

    private final AiChatRepository aiChatRepository;
    private final AiMessageRepository aiMessageRepository;
    private final UserRepository userRepository;
    private final AiService aiService;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public AiChatResponse createChat() {
        User user = getCurrentUser();
        AiChat chat = AiChat.builder()
                .title("New Chat")
                .user(user)
                .build();
        return toResponse(aiChatRepository.save(chat));
    }

    public List<AiChatResponse> getMyChats() {
        User user = getCurrentUser();
        return aiChatRepository.findByUserIdOrderByUpdatedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public Map<String, String> sendMessage(Long chatId, String message) {
        User user = getCurrentUser();
        AiChat chat = aiChatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));


        String aiResponse = aiService.getAiResponse(message).block();


        AiMessage aiMessage = AiMessage.builder()
                .chat(chat)
                .userPrompt(message)
                .aiResponse(aiResponse)
                .createdAt(LocalDateTime.now())
                .user(user)
                .build();
        aiMessageRepository.save(aiMessage);

        // اعمل title من أول رسالة
        if (chat.getMessages().isEmpty()) {
            chat.setTitle(message.length() > 30 ? message.substring(0, 30) + "..." : message);
        }
        chat.setUpdatedAt(LocalDateTime.now());
        aiChatRepository.save(chat);

        return Map.of("response", aiResponse != null ? aiResponse : "No response from AI");
    }

    public List<AiMessage> getChatMessages(Long chatId) {
        AiChat chat = aiChatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));
        return chat.getMessages();
    }

    public AiChatResponse updateStatus(Long chatId, String status) {
        AiChat chat = aiChatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));
        chat.setStatus(status);
        return toResponse(aiChatRepository.save(chat));
    }

    public void deleteChat(Long chatId) {
        aiChatRepository.deleteById(chatId);
    }

    private AiChatResponse toResponse(AiChat chat) {
        return AiChatResponse.builder()
                .id(chat.getId())
                .title(chat.getTitle())
                .status(chat.getStatus())
                .createdAt(chat.getCreatedAt())
                .updatedAt(chat.getUpdatedAt())
                .user(AiChatResponse.UserSummary.builder()
                        .id(chat.getUser().getId())
                        .fullName(chat.getUser().getFullName())
                        .email(chat.getUser().getEmail())
                        .avatarUrl(chat.getUser().getAvatarUrl())
                        .build())
                .build();
    }
}