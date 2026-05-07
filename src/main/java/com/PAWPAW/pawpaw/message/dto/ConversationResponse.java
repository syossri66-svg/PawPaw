package com.PAWPAW.pawpaw.message.dto;

import lombok.Data;

@Data
public class ConversationResponse {
    private Long id;
    private Long participantId;
    private String participantName;
    private String avatar;
    private String lastMessage;
    private int unreadCount;
}