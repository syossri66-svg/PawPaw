package com.PAWPAW.pawpaw.community.repository;

import com.PAWPAW.pawpaw.community.entity.Story;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface StoryRepository extends JpaRepository<Story, String> {

    List<Story> findByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime time);
}