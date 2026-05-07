package com.PAWPAW.pawpaw.message.service;

import com.PAWPAW.pawpaw.auth.entity.User;
import com.PAWPAW.pawpaw.auth.repository.UserRepository;
import com.PAWPAW.pawpaw.message.dto.*;
import com.PAWPAW.pawpaw.message.entity.*;
import com.PAWPAW.pawpaw.message.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<ConversationResponse> getMyConversations() {
        User user = getCurrentUser();
        return conversationRepository.findAllByUserId(user.getId())
                .stream().map(c -> {
                    User participant = c.getUser1().getId().equals(user.getId()) ? c.getUser2() : c.getUser1();
                    Message last = messageRepository.findLastMessage(c.getId());
                    int unread = messageRepository.countUnread(c.getId(), user.getId());

                    ConversationResponse res = new ConversationResponse();
                    res.setId(c.getId());
                    res.setParticipantId(participant.getId());
                    res.setParticipantName(participant.getFullName());
                    res.setAvatar(null);
                    res.setLastMessage(last != null ? last.getText() : "");
                    res.setUnreadCount(unread);
                    return res;
                }).collect(Collectors.toList());
    }

    public List<MessageResponse> getMessages(Long conversationId) {
        messageRepository.markAsRead(conversationId, getCurrentUser().getId(), MessageStatus.SEEN);
        return messageRepository.findByConversationIdOrderByTimestampAsc(conversationId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public MessageResponse sendMessage(MessageRequest request) {
        User sender = getCurrentUser();
        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Conversation conversation = conversationRepository
                .findByUsers(sender.getId(), receiver.getId())
                .orElseGet(() -> conversationRepository.save(
                        Conversation.builder().user1(sender).user2(receiver).build()));

        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .text(request.getText())
                .build();

        return mapToResponse(messageRepository.save(message));
    }

    public void deleteMessage(Long messageId) {
        messageRepository.deleteById(messageId);
    }

    private MessageResponse mapToResponse(Message m) {
        MessageResponse res = new MessageResponse();
        res.setId(m.getId());
        res.setSenderId(m.getSender().getId());
        res.setText(m.getText());
        res.setTimestamp(m.getTimestamp());
        res.setStatus(m.getStatus());
        return res;
    }
}