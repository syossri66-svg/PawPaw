package com.PAWPAW.pawpaw.group.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GroupRequest {
    @NotBlank
    private String name;
    private String description;
    private String imageUrl;
}