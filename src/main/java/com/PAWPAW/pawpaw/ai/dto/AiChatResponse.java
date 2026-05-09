package com.PAWPAW.pawpaw.ai.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class AiChatResponse {
    private Long id;
    private String title;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserSummary user;

    @Data
    @Builder
    public static class UserSummary {
        private Long id;
        private String fullName;
        private String email;
        private String avatarUrl;
    }
}