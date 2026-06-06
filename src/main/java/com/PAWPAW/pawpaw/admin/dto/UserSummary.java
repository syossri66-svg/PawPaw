package com.PAWPAW.pawpaw.admin.dto;

import com.PAWPAW.pawpaw.auth.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummary {
    private Long id;
    private String fullName;
    private String email;
    private UserRole role;
    private boolean isVerified;
    private boolean isBanned;
    private LocalDateTime createdAt;
    private String avatarUrl;
}