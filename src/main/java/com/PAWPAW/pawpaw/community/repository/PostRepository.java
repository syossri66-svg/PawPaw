package com.PAWPAW.pawpaw.community.repository;

import com.PAWPAW.pawpaw.community.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // 1. هنجيب كل البوستات ومعاها اليوزر في كويري واحد مجمع
    @Query("SELECT DISTINCT p FROM Post p JOIN FETCH p.user ORDER BY p.createdAt DESC")
    List<Post> findAllPostsWithUser();

    // 2. هنجيب بوستات يوزر معين ومعاها بياناته برضه في كويري واحد
    @Query("SELECT DISTINCT p FROM Post p JOIN FETCH p.user WHERE p.user.id = :userId ORDER BY p.createdAt DESC")
    List<Post> findByUserIdWithUser(@Param("userId") Long userId);

    // الميثودز البسيطة دي سيبها زي ما هي عادي جداً مش مسببة أزمة
    long countByUserId(Long userId);
    List<Post> findByUserId(Long userId);
    List<Post> findByUserIdOrderByCreatedAtDesc(Long userId);
}