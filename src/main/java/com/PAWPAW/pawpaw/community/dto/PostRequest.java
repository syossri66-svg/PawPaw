package com.PAWPAW.pawpaw.community.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class PostRequest {

    @NotBlank
    private String content;

    private MultipartFile image;
}