package com.PAWPAW.pawpaw.chat.service;

import com.PAWPAW.pawpaw.auth.entity.User;
import com.PAWPAW.pawpaw.auth.repository.UserRepository;
import com.PAWPAW.pawpaw.chat.dto.ConversationResponse;
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

    public long getUnreadCount(Long senderId) {
        User currentUser = getCurrentUser();
        return messageRepository.countBySenderIdAndReceiverIdAndIsReadFalse(senderId, currentUser.getId());
    }

    private MessageResponse mapToResponse(Message message) {
        MessageResponse response = new MessageResponse();
        response.setId(message.getId());
        response.setSenderId(message.getSender().getId());
        response.setText(message.getContent());
        response.setTimestamp(message.getCreatedAt());
        response.setStatus(message.isRead() ? "seen" : "sent");
        return response;
    }

    public List<ConversationResponse> getMyConversations() {
        // استخدمي الطريقة دي عشان نضمن إننا بنجيب اليوزر الصح من غير تضارب
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email;
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            email = ((org.springframework.security.core.userdetails.UserDetails)principal).getUsername();
        } else {
            email = principal.toString();
        }

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Long currentUserId = currentUser.getId();

        // نداء الـ Repository اللي لسه مصلحينه
        List<Message> messages = messageRepository.findLastMessagesForUser(currentUserId);

        return messages.stream().map(msg -> {
            User partner = msg.getSender().getId().equals(currentUserId)
                    ? msg.getReceiver()
                    : msg.getSender();

            return ConversationResponse.builder()
                    .conversationId(partner.getId())
                    .participantName(partner.getFullName())
                    .avatar(partner.getProfilePicture())
                    .lastMessage(msg.getContent())
                    .unreadCount(0) // ممكن نطورها لاحقاً
                    .isOnline(false)
                    .build();
        }).collect(Collectors.toList());
    }
    // 1. ميثود مسح الرسالة
    public void deleteMessage(Long messageId) {
        User currentUser = getCurrentUser();
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        // تأكد إن اللي بيمسح الرسالة هو اللي بعتها (حماية)
        if (!message.getSender().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only delete your own messages");
        }

        messageRepository.delete(message);
    }

    // 2. ميثود تحويل المحادثة كلها لـ Seen
    public void markConversationAsRead(Long partnerId) {
        User currentUser = getCurrentUser();
        List<Message> messages = messageRepository.findConversation(currentUser.getId(), partnerId);

        messages.stream()
                .filter(m -> m.getReceiver().getId().equals(currentUser.getId()) && !m.isRead())
                .forEach(m -> {
                    m.setRead(true);
                    messageRepository.save(m);
                });
    }
}