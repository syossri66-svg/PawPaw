package com.PAWPAW.pawpaw.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReelResponse {
    private String id;
    private String videoUrl;
    private String title;
    private Long viewsCount;
    private String thumbnailUrl;
}