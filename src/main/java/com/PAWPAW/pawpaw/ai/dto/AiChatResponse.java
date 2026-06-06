package com.PAWPAW.pawpaw.ai.dto;

import com.PAWPAW.pawpaw.admin.dto.UserSummary;
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

    private String aiResponse;
}