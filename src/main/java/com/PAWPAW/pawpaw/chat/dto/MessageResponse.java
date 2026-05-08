package com.PAWPAW.pawpaw.chat.dto;


import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MessageResponse {
    private Long id;

    private Long senderId;
    private String text;
    private LocalDateTime timestamp;
    private String status;
}