package com.PAWPAW.pawpaw.ai.service;

import com.PAWPAW.pawpaw.admin.dto.UserSummary;
import com.PAWPAW.pawpaw.auth.entity.User;
import com.PAWPAW.pawpaw.auth.repository.UserRepository;
import com.PAWPAW.pawpaw.ai.dto.AiChatResponse;
import com.PAWPAW.pawpaw.ai.entity.AiChat;
import com.PAWPAW.pawpaw.ai.entity.AiMessage;
import com.PAWPAW.pawpaw.ai.repository.AiChatRepository;
import com.PAWPAW.pawpaw.ai.repository.AiMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Transactional
    public AiChatResponse createChat() {
        User currentUser = getCurrentUser();
        AiChat chat = AiChat.builder()
                .title("New Chat")
                .status("ACTIVE")
                .user(currentUser)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return mapToAiChatResponse(aiChatRepository.save(chat), currentUser, null);
    }

    public List<AiChatResponse> getMyChats() {
        User currentUser = getCurrentUser();
        return aiChatRepository.findByUserIdOrderByUpdatedAtDesc(currentUser.getId())
                .stream()
                .map(chat -> mapToAiChatResponse(chat, currentUser, null))
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getChatMessages(Long chatId) {
        AiChat chat = aiChatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        return chat.getMessages().stream()
                .map(msg -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("userPrompt", msg.getUserPrompt());
                    map.put("aiResponse", msg.getAiResponse());
                    map.put("createdAt", msg.getCreatedAt());
                    return map;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public AiChatResponse updateStatus(Long chatId, String status) {
        User currentUser = getCurrentUser();
        AiChat chat = aiChatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Not found"));
        chat.setStatus(status);
        chat.setUpdatedAt(LocalDateTime.now());
        return mapToAiChatResponse(aiChatRepository.save(chat), currentUser, null);
    }

    @Transactional
    public void deleteChat(Long chatId) {
        aiChatRepository.deleteById(chatId);
    }

    @Transactional
    public Map<String, Object> sendMessage(Long chatId, String content) {
        User currentUser = getCurrentUser();
        AiChat chat = aiChatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        String aiAnswer = aiService.getAiResponse(content).block();

        AiMessage message = AiMessage.builder()
                .chat(chat)
                .user(currentUser)
                .userPrompt(content)
                .aiResponse(aiAnswer)
                .createdAt(LocalDateTime.now())
                .build();
        aiMessageRepository.save(message);

        if (chat.getTitle() == null || "New Chat".equalsIgnoreCase(chat.getTitle().trim())) {
            chat.setTitle(generateTitleFromQuestion(content));
        }
        chat.setUpdatedAt(LocalDateTime.now());
        aiChatRepository.save(chat);

        Map<String, Object> response = new HashMap<>();
        response.put("response", aiAnswer);
        response.put("id", message.getId());
        response.put("createdAt", message.getCreatedAt());
        response.put("title", chat.getTitle());
        return response;
    }

    private String generateTitleFromQuestion(String question) {
        String[] words = question.trim().split("\\s+");
        String title = words[0] + (words.length > 1 ? " " + words[1] : "");
        return title.length() > 20 ? title.substring(0, 20) + "..." : title;
    }

    private AiChatResponse mapToAiChatResponse(AiChat chat, User user, String aiAnswer) {
        // 🔥 تم التعديل هنا: استخدام الـ UserSummary المستقل
        return AiChatResponse.builder()
                .id(chat.getId())
                .title(chat.getTitle())
                .status(chat.getStatus())
                .createdAt(chat.getCreatedAt())
                .updatedAt(chat.getUpdatedAt())
                .aiResponse(aiAnswer)
                .user(UserSummary.builder()
                        .id(user.getId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .avatarUrl(user.getAvatarUrl())
                        .build())
                .build();
    }
}