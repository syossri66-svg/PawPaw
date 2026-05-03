package com.PAWPAW.pawpaw.ai.repository;

import com.PAWPAW.pawpaw.ai.entity.AiMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface AiMessageRepository extends JpaRepository<AiMessage, UUID> {


    @Query("SELECT m FROM AiMessage m WHERE m.user.id = :userId ORDER BY m.createdAt DESC")
    List<AiMessage> findMessagesByUserId(@Param("userId") Long userId);}