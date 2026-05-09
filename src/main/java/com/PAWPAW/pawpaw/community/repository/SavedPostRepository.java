package com.PAWPAW.pawpaw.community.repository;

import com.PAWPAW.pawpaw.community.entity.SavedPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SavedPostRepository extends JpaRepository<SavedPost, Long> {
    Optional<SavedPost> findByUserIdAndPostId(Long userId, Long postId);
    List<SavedPost> findByUserId(Long userId);
}