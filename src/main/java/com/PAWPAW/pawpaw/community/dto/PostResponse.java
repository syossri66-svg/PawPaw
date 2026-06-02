package com.PAWPAW.pawpaw.community.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponse {


    private String id;
    private String content;
    private String mediaUrl;
    private LocalDateTime createdAt;
    private AuthorDto author;
    private long likesCount;
    private boolean liked;
    private List<CommentResponse> comments;

    private String title;
    private UserInfo user;
    private ContentInfo contentInfo;
    private StatsInfo stats;
    private boolean saved;


    public void setContent(ContentInfo contentInfo) {
        this.contentInfo = contentInfo;
        if (contentInfo != null) {
            this.content = contentInfo.getText();
            this.mediaUrl = contentInfo.getMediaUrl();
        }
    }

    public void setId(Long id) {
        this.id = id != null ? id.toString() : null;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuthorDto {
        private String username;
        private String profilePicture;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentDto {
        private String text;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserInfo {
        private Long id;
        private String name;
        private String avatar;
        private boolean following;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContentInfo {
        private String text;
        private String mediaUrl;
        private String type;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StatsInfo {

        private long likes;
        private long comments;
        private long shares;


        public long getLikesCount() { return this.likes; }
        public long getCommentsCount() { return this.comments; }
        public long getSharesCount() { return this.shares; }
    }
}