package com.PAWPAW.pawpaw.appointment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppointmentRequest {

    @NotNull
    private Long petId;

    @NotNull
    private Long vetId;

    @NotNull
    private LocalDateTime scheduledAt;

    private String reason;
}