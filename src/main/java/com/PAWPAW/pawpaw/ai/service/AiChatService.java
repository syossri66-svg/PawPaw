package com.PAWPAW.pawpaw.ai.service;

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

    public AiChat createChat() {
        User user = getCurrentUser();
        AiChat chat = AiChat.builder()
                .title("New Chat")
                .user(user)
                .build();
        return aiChatRepository.save(chat);
    }

    public List<AiChat> getMyChats() {
        User user = getCurrentUser();
        return aiChatRepository.findByUserIdOrderByUpdatedAtDesc(user.getId());
    }

    public Map<String, String> sendMessage(Long chatId, String message) {
        User user = getCurrentUser();
        AiChat chat = aiChatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        // بعتي للـ AI
        String aiResponse = aiService.getAiResponse(message).block();

        // احفظي الرسالة
        AiMessage aiMessage = AiMessage.builder()
                .chat(chat)
                .userPrompt(message)
                .aiResponse(aiResponse)
                .createdAt(LocalDateTime.now())
                .user(user)
                .build();
        aiMessageRepository.save(aiMessage);

        // لو أول رسالة — حدثي العنوان
        if (chat.getMessages().size() == 0) {
            chat.setTitle(message.length() > 30 ? message.substring(0, 30) + "..." : message);
        }
        chat.setUpdatedAt(LocalDateTime.now());
        aiChatRepository.save(chat);

        return Map.of("response", aiResponse);
    }

    public List<AiMessage> getChatMessages(Long chatId) {
        AiChat chat = aiChatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));
        return chat.getMessages();
    }

    public AiChat updateStatus(Long chatId, String status) {
        AiChat chat = aiChatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));
        chat.setStatus(status);
        return aiChatRepository.save(chat);
    }

    public void deleteChat(Long chatId) {
        aiChatRepository.deleteById(chatId);
    }
}