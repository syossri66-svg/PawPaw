package com.PAWPAW.pawpaw.ai.service;

import com.PAWPAW.pawpaw.auth.entity.User;
import com.PAWPAW.pawpaw.auth.repository.UserRepository;
import com.PAWPAW.pawpaw.ai.dto.AiChatResponse;
import com.PAWPAW.pawpaw.ai.entity.AiChat; // تأكد من اسم الباكيدج والـ Entity للـ Chat عندك
import com.PAWPAW.pawpaw.ai.repository.AiChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AiChatService {

    private final AiChatRepository aiChatRepository;
    private final UserRepository userRepository;
    // private final OpenAiService openAiService; // الـ Service اللي بتكلم بيه الـ LLM عندك

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public AiChatResponse askAi(Long chatId, String userQuestion) {
        User currentUser = getCurrentUser();

        // 1. جلب الشات من الداتا بيز
        AiChat chat = aiChatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("AI Chat session not found"));

        // 2. مناداة الـ AI الفعلي عشان يجيب الإجابة (شغل كودك القديم هنا عادي)
        // String aiAnswer = openAiService.generateResponse(userQuestion);
        String aiAnswer = "إجابة الـ AI التجريبية";

        // 3. 🔥 الـ Auto-Naming السحري:
        // لو الشات لسه جديد، أو اسمه فاضي، أو بيساوي "New Chat" الافتراضية، بنغير الاسم فوراً بناءً على السؤال
        if (chat.getTitle() == null || "New Chat".equalsIgnoreCase(chat.getTitle().trim()) || chat.getTitle().trim().isEmpty()) {
            String smartTitle = generateTitleFromQuestion(userQuestion);
            chat.setTitle(smartTitle);
            chat.setUpdatedAt(LocalDateTime.now());
            aiChatRepository.save(chat); // حفظ الاسم الجديد في الداتا بيز
        }

        // 4. تحويل الـ Entity لـ الـ DTO المظبوط بتاعك اللي الفرونت مستنياه
        return mapToAiChatResponse(chat, currentUser);
    }

    // ميثود ذكية لتقطيع أول 4 كلمات من سؤال اليوزر عشان نعمل عنوان رايق للشات مع الـ AI
    private String generateTitleFromQuestion(String question) {
        if (question == null || question.trim().isEmpty()) {
            return "استشارة طبية ذكية";
        }

        String[] words = question.trim().split("\\s+");
        int wordsToTake = Math.min(words.length, 4); // هناخد أول 4 كلمات بالظبط

        StringBuilder titleBuilder = new StringBuilder();
        for (int i = 0; i < wordsToTake; i++) {
            titleBuilder.append(words[i]).append(" ");
        }

        String title = titleBuilder.toString().trim();

        // لو سؤال اليوزر أطول من الـ 4 كلمات، بنضيف في الآخر نقط عشان الشكل الجمالي (...)
        return question.length() > title.length() ? title + "..." : title;
    }

    // ميثود الـ Mapping المبنية على الـ DTO الحقيقي بتاعك بالملي
    private AiChatResponse mapToAiChatResponse(AiChat chat, User user) {
        return AiChatResponse.builder()
                .id(chat.getId())
                .title(chat.getTitle()) // الـ Title الجديد أو المستنتج هيرجع هنا تلقائي لمنة
                .status(chat.getStatus())
                .createdAt(chat.getCreatedAt())
                .updatedAt(chat.getUpdatedAt())
                .user(AiChatResponse.UserSummary.builder()
                        .id(user.getId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .avatarUrl(user.getAvatarUrl())
                        .build())
                .build();
    }
}