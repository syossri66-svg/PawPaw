package com.PAWPAW.pawpaw.ai.service;

import com.PAWPAW.pawpaw.auth.entity.User;
import com.PAWPAW.pawpaw.auth.repository.UserRepository;
import com.PAWPAW.pawpaw.ai.dto.AiChatResponse;
import com.PAWPAW.pawpaw.ai.entity.AiChat;
import com.PAWPAW.pawpaw.ai.repository.AiChatRepository;
// تأكد إنك عامل Import لخدمة الـ AI الخاصة بيك هنا
// import com.PAWPAW.pawpaw.ai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiChatService {

    private final AiChatRepository aiChatRepository;
    private final UserRepository userRepository;
    private final AiService aiService;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public AiChatResponse askAi(Long chatId, String userQuestion) {
        User currentUser = getCurrentUser();
        AiChat chat = aiChatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("AI Chat session not found"));

        // 🟢 الرجوع للـ Logic الأصلي: هنا المفروض تنادي الـ Service اللي بيجيب الرد الحقيقي
        // String aiAnswer = openAiService.generateResponse(userQuestion);
        String aiAnswer = aiService.getAiResponse(userQuestion).block(); // ✅ بدل الـ hardcoded

        // الـ Auto-Naming (موجود زي ما هو عشان منة متضايقش)
        if (chat.getTitle() == null || "New Chat".equalsIgnoreCase(chat.getTitle().trim()) || chat.getTitle().trim().isEmpty()) {
            chat.setTitle(generateTitleFromQuestion(userQuestion));
            chat.setUpdatedAt(LocalDateTime.now());
            aiChatRepository.save(chat);
        }

        return mapToAiChatResponse(chat, currentUser, aiAnswer);
    }

    // باقي الميثودز (createChat, getMyChats, etc.) بتفضل زي ما هي
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

    public AiChatResponse getChatMessages(Long chatId) {
        User currentUser = getCurrentUser();
        AiChat chat = aiChatRepository.findById(chatId).orElseThrow(() -> new RuntimeException("Not found"));
        return mapToAiChatResponse(chat, currentUser, null);
    }

    @Transactional
    public AiChatResponse updateStatus(Long chatId, String status) {
        User currentUser = getCurrentUser();
        AiChat chat = aiChatRepository.findById(chatId).orElseThrow(() -> new RuntimeException("Not found"));
        chat.setStatus(status);
        chat.setUpdatedAt(LocalDateTime.now());
        return mapToAiChatResponse(aiChatRepository.save(chat), currentUser, null);
    }

    @Transactional
    public void deleteChat(Long chatId) {
        aiChatRepository.deleteById(chatId);
    }

    @Transactional
    public AiChatResponse sendMessage(Long chatId, String content) {
        return askAi(chatId, content);
    }

    private String generateTitleFromQuestion(String question) {
        String[] words = question.trim().split("\\s+");
        String title = words[0] + (words.length > 1 ? " " + words[1] : "");
        return title.length() > 20 ? title.substring(0, 20) + "..." : title;
    }

    private AiChatResponse mapToAiChatResponse(AiChat chat, User user, String aiAnswer) {
        return AiChatResponse.builder()
                .id(chat.getId())
                .title(chat.getTitle())
                .status(chat.getStatus())
                .createdAt(chat.getCreatedAt())
                .updatedAt(chat.getUpdatedAt())
                .aiResponse(aiAnswer)
                .user(AiChatResponse.UserSummary.builder()
                        .id(user.getId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .avatarUrl(user.getAvatarUrl())
                        .build())
                .build();
    }
}