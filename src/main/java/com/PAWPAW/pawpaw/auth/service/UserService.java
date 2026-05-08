package com.PAWPAW.pawpaw.auth.service;

import com.PAWPAW.pawpaw.auth.dto.UserStatsResponse;
import com.PAWPAW.pawpaw.auth.entity.User;
import com.PAWPAW.pawpaw.auth.repository.UserRepository;
import com.PAWPAW.pawpaw.community.entity.Follow;
import com.PAWPAW.pawpaw.community.repository.FollowRepository;
import com.PAWPAW.pawpaw.community.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final PostRepository postRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public UserStatsResponse getUserStats(Long userId) {
        UserStatsResponse stats = new UserStatsResponse();
        stats.setFollowers(followRepository.countByFollowingId(userId));
        stats.setFollowing(followRepository.countByFollowerId(userId));
        stats.setPosts(postRepository.countByUserId(userId));
        return stats;
    }

    public String toggleFollow(Long targetId) {
        User current = getCurrentUser();
        User target = userRepository.findById(targetId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return followRepository.findByFollowerIdAndFollowingId(current.getId(), targetId)
                .map(f -> { followRepository.delete(f); return "Unfollowed"; })
                .orElseGet(() -> {
                    followRepository.save(Follow.builder()
                            .follower(current).following(target).build());
                    return "Followed";
                });
    }

    public boolean isFollowing(Long targetId) {
        User current = getCurrentUser();
        return followRepository.findByFollowerIdAndFollowingId(current.getId(), targetId).isPresent();
    }
}