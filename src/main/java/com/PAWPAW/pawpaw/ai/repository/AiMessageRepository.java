package com.PAWPAW.pawpaw.ai.repository;

import com.PAWPAW.pawpaw.ai.entity.AiMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface AiMessageRepository extends JpaRepository<AiMessage, UUID> {


    List<AiMessage> findByUser_IdOrderByCreatedAtDesc(UUID userId);
}