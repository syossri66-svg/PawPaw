package com.PAWPAW.pawpaw.chat.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MessageRequest {

    @NotNull
    private Long receiverId;

    @NotBlank
    private String content;
}
