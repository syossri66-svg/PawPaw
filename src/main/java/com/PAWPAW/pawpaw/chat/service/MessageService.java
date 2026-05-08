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

    // دي الميثود اللي سيبناها عشان الفرونت محتاجها
    public List<ConversationResponse> getMyConversations() {
        User currentUser = getCurrentUser();

        return messageRepository.findChatPartners(currentUser.getId())
                .stream()
                .map(obj -> {
                    User partner = (User) obj;
                    List<Message> conv = messageRepository.findConversation(currentUser.getId(), partner.getId());

                    // تأمين الكود لو مفيش رسايل خالص
                    if (conv.isEmpty()) return null;

                    Message lastMsg = conv.get(conv.size() - 1);
                    long unread = messageRepository.countBySenderIdAndReceiverIdAndIsReadFalse(partner.getId(), currentUser.getId());

                    return ConversationResponse.builder()
                            .conversationId(partner.getId())
                            .participantName(partner.getFullName())
                            .avatar(partner.getProfilePicture())
                            .lastMessage(lastMsg.getContent())
                            .unreadCount(unread)
                            .isOnline(false)
                            .build();
                })
                .filter(c -> c != null)
                .collect(Collectors.toList());
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