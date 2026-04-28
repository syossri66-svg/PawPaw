package com.PAWPAW.pawpaw.chat.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MessageResponse {
    private Long id;
    private Long senderId;
    private String senderName;
    private Long receiverId;
    private String receiverName;
    private String content;
    private boolean isRead;
    private LocalDateTime createdAt;
}