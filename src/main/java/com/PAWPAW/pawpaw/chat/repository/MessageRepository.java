package com.PAWPAW.pawpaw.chat.repository;

import com.PAWPAW.pawpaw.chat.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m WHERE (m.sender.id = :userId1 AND m.receiver.id = :userId2) OR (m.sender.id = :userId2 AND m.receiver.id = :userId1) ORDER BY m.createdAt ASC")
    List<Message> findConversation(Long userId1, Long userId2);

    @Query("SELECT DISTINCT CASE WHEN m.sender.id = :userId THEN m.receiver ELSE m.sender END FROM Message m WHERE m.sender.id = :userId OR m.receiver.id = :userId")
    List<Object> findChatPartners(Long userId);

    long countBySenderIdAndReceiverIdAndIsReadFalse(Long senderId, Long receiverId);

    @Query("SELECT m FROM Message m WHERE m.id IN (" +
            "SELECT MAX(m2.id) FROM Message m2 " +
            "WHERE m2.sender.id = :userId OR m2.receiver.id = :userId " +
            "GROUP BY CASE " +
            "  WHEN m2.sender.id = :userId THEN m2.receiver.id " +
            "  ELSE m2.sender.id END)")
    List<Message> findLastMessagesForUser(@Param("userId") Long userId);
}