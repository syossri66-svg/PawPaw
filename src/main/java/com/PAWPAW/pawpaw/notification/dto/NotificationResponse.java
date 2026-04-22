package com.PAWPAW.pawpaw.notification.dto;

import com.PAWPAW.pawpaw.notification.entity.NotificationType;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationResponse {
    private Long id;
    private String title;
    private String message;
    private boolean isRead;
    private NotificationType type;
    private LocalDateTime createdAt;
}