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

    // ميثود تحويل الرسالة لشكل الـ Response الجديد
    private MessageResponse mapToResponse(Message message) {
        MessageResponse response = new MessageResponse();
        response.setId(message.getId());
        response.setSenderId(message.getSender().getId());
        response.setText(message.getContent()); // تحويل content لـ text
        response.setTimestamp(message.getCreatedAt()); // تحويل createdAt لـ timestamp
        response.setStatus(message.isRead() ? "seen" : "sent"); // تحويل boolean لـ String
        return response;
    }

    // ميثود تجيب كل المحادثات بالتفاصيل اللي الفرونت عايزاها
    public List<ConversationResponse> getMyConversations() {
        User currentUser = getCurrentUser();

        // بنجيب كل الناس اللي بعتنا لهم أو بعتوا لنا
        return messageRepository.findChatPartners(currentUser.getId())
                .stream()
                .map(obj -> {
                    User partner = (User) obj;
                    // بنجيب آخر رسالة بيني وبين الشخص ده
                    List<Message> conv = messageRepository.findConversation(currentUser.getId(), partner.getId());
                    Message lastMsg = conv.get(conv.size() - 1);

                    // بنحسب عدد الرسائل اللي الشخص ده بعتها لي وأنا لسه مقرتهاش
                    long unread = messageRepository.countBySenderIdAndReceiverIdAndIsReadFalse(partner.getId(), currentUser.getId());

                    return ConversationResponse.builder()
                            .conversationId(partner.getId())
                            .participantName(partner.getFullName())
                            .avatar(partner.getProfilePicture()) // تأكدي إن عندك الحقل ده في موديل الـ User
                            .lastMessage(lastMsg.getContent())
                            .unreadCount(unread)
                            .isOnline(false) // دي ممكن تتظبط لاحقاً مع الـ Sockets
                            .build();
                })
                .collect(Collectors.toList());
    }
}