package com.PAWPAW.pawpaw.auth.dto;

import com.PAWPAW.pawpaw.auth.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private UserRole role;
    private boolean isVerified;
    private String bio;
    private String location;


    private String username;
    private String profilePicture;
    private String coverPhoto;


    private String born;
    private String status;
    private String education;
    private String graduationYear;
    private int followersCount;
    private int followingCount;
    private int postsCount;
}