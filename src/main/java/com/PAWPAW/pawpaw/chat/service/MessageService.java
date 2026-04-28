package com.PAWPAW.pawpaw.chat.service;

import com.PAWPAW.pawpaw.auth.entity.User;
import com.PAWPAW.pawpaw.auth.repository.UserRepository;
import com.PAWPAW.pawpaw.chat.dto.MessageRequest;
import com.PAWPAW.pawpaw.chat.dto.MessageResponse;
import com.PAWPAW.pawpaw.chat.entity.Message;
import com.PAWPAW.pawpaw.chat.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public MessageResponse sendMessage(MessageRequest request) {
        User sender = getCurrentUser();
        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        Message message = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .content(request.getContent())
                .isRead(false)
                .build();

        return mapToResponse(messageRepository.save(message));
    }

    public List<MessageResponse> getConversation(Long otherUserId) {
        User currentUser = getCurrentUser();
        List<Message> messages = messageRepository.findConversation(currentUser.getId(), otherUserId);

        messages.stream()
                .filter(m -> m.getReceiver().getId().equals(currentUser.getId()) && !m.isRead())
                .forEach(m -> {
                    m.setRead(true);
                    messageRepository.save(m);
                });

        return messages.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<MessageResponse> getMyConversations() {
        User currentUser = getCurrentUser();
        return messageRepository.findChatPartners(currentUser.getId())
                .stream()
                .filter(obj -> obj instanceof User)
                .map(obj -> {
                    User partner = (User) obj;
                    List<Message> conv = messageRepository.findConversation(currentUser.getId(), partner.getId());
                    if (conv.isEmpty()) return null;
                    return mapToResponse(conv.get(conv.size() - 1));
                })
                .filter(m -> m != null)
                .collect(Collectors.toList());
    }

    public long getUnreadCount(Long senderId) {
        User currentUser = getCurrentUser();
        return messageRepository.countBySenderIdAndReceiverIdAndIsReadFalse(senderId, currentUser.getId());
    }

    private MessageResponse mapToResponse(Message message) {
        MessageResponse response = new MessageResponse();
        response.setId(message.getId());
        response.setSenderId(message.getSender().getId());
        response.setSenderName(message.getSender().getFullName());
        response.setReceiverId(message.getReceiver().getId());
        response.setReceiverName(message.getReceiver().getFullName());
        response.setContent(message.getContent());
        response.setRead(message.isRead());
        response.setCreatedAt(message.getCreatedAt());
        return response;
    }
}