package com.PAWPAW.pawpaw.community.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PostResponse {
    private Long id;
    private String content;
    private String imageUrl;
    private Long userId;
    private String userName;
    private int likesCount;
    private int commentsCount;
    private LocalDateTime createdAt;
}