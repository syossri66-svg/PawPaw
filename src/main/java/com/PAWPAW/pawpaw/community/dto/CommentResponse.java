package com.PAWPAW.pawpaw.community.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommentResponse {
    private Long id;
    private String content;
    private Long userId;
    private String userName;
    private Long postId;
    private LocalDateTime createdAt;
}