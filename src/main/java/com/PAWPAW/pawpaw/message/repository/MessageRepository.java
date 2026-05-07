package com.PAWPAW.pawpaw.message.repository;

import com.PAWPAW.pawpaw.message.entity.Message;
import com.PAWPAW.pawpaw.message.entity.MessageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByConversationIdOrderByTimestampAsc(Long conversationId);

    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.status = :status WHERE m.conversation.id = :convId AND m.sender.id != :userId")
    void markAsRead(@Param("convId") Long convId, @Param("userId") Long userId, @Param("status") MessageStatus status);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation.id = :convId AND m.status = 'SENT' AND m.sender.id != :userId")
    int countUnread(@Param("convId") Long convId, @Param("userId") Long userId);

    @Query("SELECT m FROM Message m WHERE m.conversation.id = :convId ORDER BY m.timestamp DESC LIMIT 1")
    Message findLastMessage(@Param("convId") Long convId);
}