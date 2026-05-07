package com.PAWPAW.pawpaw.message.dto;

import com.PAWPAW.pawpaw.message.entity.MessageStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MessageResponse {
    private Long id;
    private Long senderId;
    private String text;
    private LocalDateTime timestamp;
    private MessageStatus status;
}