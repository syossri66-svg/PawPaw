package com.PAWPAW.pawpaw.chat.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConversationResponse {
    private Long conversationId;
    private String participantName;
    private String avatar;
    private String lastMessage;
    private long unreadCount;
    private boolean isOnline;
}