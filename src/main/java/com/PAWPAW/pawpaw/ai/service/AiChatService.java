package com.PAWPAW.pawpaw.ai.service;

import com.PAWPAW.pawpaw.auth.entity.User;
import com.PAWPAW.pawpaw.auth.repository.UserRepository;
import com.PAWPAW.pawpaw.ai.dto.AiChatResponse;
import com.PAWPAW.pawpaw.ai.entity.AiChat;
import com.PAWPAW.pawpaw.ai.repository.AiChatRepository;
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

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // 1. الميثود الأساسية لإرسال السؤال والرد مع الـ Auto-Naming
    @Transactional
    public AiChatResponse askAi(Long chatId, String userQuestion) {
        User currentUser = getCurrentUser();

        AiChat chat = aiChatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("AI Chat session not found"));

        // شغل كود الـ AI الفعلي بتاعك هنا (أو نداء الـ OpenAiService)
        String aiAnswer = "إجابة الـ AI على سؤالك عن أليفك!";

        // الـ Auto-Naming السحري
        if (chat.getTitle() == null || "New Chat".equalsIgnoreCase(chat.getTitle().trim()) || chat.getTitle().trim().isEmpty()) {
            String smartTitle = generateTitleFromQuestion(userQuestion);
            chat.setTitle(smartTitle);
            chat.setUpdatedAt(LocalDateTime.now());
            aiChatRepository.save(chat);
        }

        return mapToAiChatResponse(chat, currentUser, aiAnswer);
    }

    // 2. إنشاء شات جديد مع الـ AI (بيبدأ باسم New Chat افتراضياً)
    @Transactional
    public AiChatResponse createChat() {
        User currentUser = getCurrentUser();

        AiChat chat = AiChat.builder()
                .title("New Chat")
                .status("ACTIVE")
                .user(currentUser) // لو الـ Entity عندك بتربط بالـ User مباشرة
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        AiChat savedChat = aiChatRepository.save(chat);
        return mapToAiChatResponse(savedChat, currentUser, null);
    }

    // 3. جلب كل شاتات المستخدم الحالي (عشان الـ Sidebar)
    public List<AiChatResponse> getMyChats() {
        User currentUser = getCurrentUser();
        // افترضنا إن عندك ميثود في الـ repository بتجيب الشات بالـ userId أو بالـ User entity
        return aiChatRepository.findByUserIdOrderByUpdatedAtDesc(currentUser.getId())
                .stream()
                .map(chat -> mapToAiChatResponse(chat, currentUser, null))
                .collect(Collectors.toList());
    }

    // 4. جلب رسائل شات معين للـ AI
    public AiChatResponse getChatMessages(Long chatId) {
        User currentUser = getCurrentUser();
        AiChat chat = aiChatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("AI Chat session not found"));

        return mapToAiChatResponse(chat, currentUser, null);
    }

    // 5. تحديث حالة الشات (لو منة بتستخدمها)
    @Transactional
    public AiChatResponse updateStatus(Long chatId, String status) {
        User currentUser = getCurrentUser();
        AiChat chat = aiChatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("AI Chat session not found"));

        chat.setStatus(status);
        chat.setUpdatedAt(LocalDateTime.now());
        return mapToAiChatResponse(aiChatRepository.save(chat), currentUser, null);
    }

    // 6. مسح الشات
    @Transactional
    public void deleteChat(Long chatId) {
        aiChatRepository.deleteById(chatId);
    }

    // 7. ميثود إضافية لـ sendMessage لو الكنترولر بينادي عليها كبديل لـ askAi
    @Transactional
    public AiChatResponse sendMessage(Long chatId, String content) {
        return askAi(chatId, content);
    }

    // ميثود توليد العنوان الذكي
    private String generateTitleFromQuestion(String question) {
        if (question == null || question.trim().isEmpty()) {
            return "استشارة طبية ذكية";
        }

        String[] words = question.trim().split("\\s+");
        int wordsToTake = Math.min(words.length, 4);

        StringBuilder titleBuilder = new StringBuilder();
        for (int i = 0; i < wordsToTake; i++) {
            titleBuilder.append(words[i]).append(" ");
        }

        String title = titleBuilder.toString().trim();
        return question.length() > title.length() ? title + "..." : title;
    }

    // ميثود الـ Mapping
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