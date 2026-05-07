package com.PAWPAW.pawpaw.message.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    // conversationId → List of sessions
    private final Map<String, List<WebSocketSession>> rooms = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String conversationId = getConversationId(session);
        rooms.computeIfAbsent(conversationId, k -> new ArrayList<>()).add(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String conversationId = getConversationId(session);
        List<WebSocketSession> sessionList = rooms.getOrDefault(conversationId, List.of());
        for (WebSocketSession s : sessionList) {
            if (s.isOpen() && !s.getId().equals(session.getId())) {
                s.sendMessage(message);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String conversationId = getConversationId(session);
        List<WebSocketSession> sessionList = rooms.get(conversationId);
        if (sessionList != null) sessionList.remove(session);
    }

    private String getConversationId(WebSocketSession session) {
        String query = session.getUri().getQuery(); // conversationId=5
        return query != null ? query.replace("conversationId=", "") : "default";
    }
}