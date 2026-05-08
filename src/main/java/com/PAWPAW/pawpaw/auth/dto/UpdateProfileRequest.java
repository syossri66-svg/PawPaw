package com.PAWPAW.pawpaw.auth.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String fullName;
    private String bio;
    private String avatarUrl;
    private String coverUrl;
    private String location;
}