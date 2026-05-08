package com.PAWPAW.pawpaw.community.repository;

import com.PAWPAW.pawpaw.community.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    Optional<Follow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);
    long countByFollowingId(Long followingId);
    long countByFollowerId(Long followerId);
}