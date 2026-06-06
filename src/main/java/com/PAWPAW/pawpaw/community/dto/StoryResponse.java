package com.PAWPAW.pawpaw.community.dto;

import com.PAWPAW.pawpaw.admin.dto.UserSummary;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StoryResponse {
    private String id;
    private UserSummary user;
    private String mediaUrl;
    private String caption;
    private LocalDateTime createdAt;
}