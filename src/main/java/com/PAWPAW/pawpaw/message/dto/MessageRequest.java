package com.PAWPAW.pawpaw.message.dto;

import lombok.Data;

@Data
public class MessageRequest {
    private Long receiverId;
    private String text;
}