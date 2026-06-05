package com.PAWPAW.pawpaw.community.repository;

import com.PAWPAW.pawpaw.community.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // 1. جلب كل البوستات لصفحة الـ Feed مع بيانات الـ User في كويري واحد مجمع
    @Query("SELECT DISTINCT p FROM Post p JOIN FETCH p.user ORDER BY p.createdAt DESC")
    List<Post> findAllPostsWithUser();

    // 2. جلب بوستات مستخدم معين باستخدام الـ ID بتاعه مع الـ FETCH لبياناته
    @Query("SELECT DISTINCT p FROM Post p JOIN FETCH p.user WHERE p.user.id = :userId ORDER BY p.createdAt DESC")
    List<Post> findByUserIdWithUser(@Param("userId") Long userId);

    // 3. لو منة بتبعت الـ Full Name / Username في الـ URL (حل مشكلة الصفحة الفاضية)
    @Query("SELECT DISTINCT p FROM Post p JOIN FETCH p.user WHERE p.user.fullName = :username ORDER BY p.createdAt DESC")
    List<Post> findByUsernameWithUser(@Param("username") String username);

    // الميثودز البسيطة اللي كانت عندك سيبناها زي ما هي عشان لو مستخدمها في مكان تاني
    long countByUserId(Long userId);
    List<Post> findByUserId(Long userId);
    List<Post> findByUserIdOrderByCreatedAtDesc(Long userId);
}