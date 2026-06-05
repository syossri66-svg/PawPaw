package com.PAWPAW.pawpaw.community.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileResponse {
    private Long id;
    private String fullName;
    private String email;
    private String avatarUrl;
    private String coverUrl;
    private String bio;
    private String location;
    private long followersCount;
    private long followingCount;
    private boolean isFollowing;
}