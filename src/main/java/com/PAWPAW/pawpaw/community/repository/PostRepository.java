package com.PAWPAW.pawpaw.community.repository;

import com.PAWPAW.pawpaw.community.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByOrderByCreatedAtDesc();
    List<Post> findByUserId(Long userId);
    long countByUserId(Long userId);
    List<Post> findByUserIdOrderByCreatedAtDesc(Long userId);
}