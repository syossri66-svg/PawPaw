package com.PAWPAW.pawpaw.group.dto;

import com.PAWPAW.pawpaw.community.dto.PostResponse;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class GroupResponse {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private Long creatorId;
    private String creatorName;
    private int membersCount;
    private LocalDateTime createdAt;
    private List<PostResponse> posts;


}