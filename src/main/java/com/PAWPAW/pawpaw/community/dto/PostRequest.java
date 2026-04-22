package com.PAWPAW.pawpaw.community.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PostRequest {

    @NotBlank
    private String content;

    private String imageUrl;
}