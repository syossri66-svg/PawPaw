package com.PAWPAW.pawpaw.auth.dto;

import com.PAWPAW.pawpaw.auth.entity.UserRole;
import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private UserRole role;
    private boolean isVerified;
}