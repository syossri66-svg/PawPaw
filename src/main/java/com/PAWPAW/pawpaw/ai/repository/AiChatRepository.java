package com.PAWPAW.pawpaw.ai.repository;

import com.PAWPAW.pawpaw.ai.entity.AiChat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AiChatRepository extends JpaRepository<AiChat, Long> {
    List<AiChat> findByUserIdOrderByUpdatedAtDesc(Long userId);
    List<AiChat> findByUserIdAndStatusNotOrderByUpdatedAtDesc(Long userId, String status);
}