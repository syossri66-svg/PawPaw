package com.PAWPAW.pawpaw.auth.dto;

import lombok.Data;

@Data
public class UserStatsResponse {
    private long followers;
    private long following;
    private long posts;
}