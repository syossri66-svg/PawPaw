package com.PAWPAW.pawpaw.community.dto;

import lombok.Data;
import java.time.LocalDateTime;
@Data
public class PostResponse {
    private Long id;
    private UserInfo user;
    private String content;
    private StatsInfo stats;
    private boolean isLiked;
    private boolean isSaved;
    private LocalDateTime createdAt;

    @Data
    public static class UserInfo {
        private Long id;
        private String name;
        private String avatar;
        private boolean isFollowing;
    }

    @Data
    public static class ContentInfo {
        private String text;
        private String mediaUrl;
        private String type;
    }

    @Data
    public static class StatsInfo {
        private int likes;
        private int comments;
        private int shares;
    }
}