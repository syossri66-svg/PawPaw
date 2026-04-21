package com.PAWPAW.pawpaw.appointment.dto;

import com.PAWPAW.pawpaw.appointment.entity.AppointmentStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppointmentResponse {
    private Long id;
    private Long petId;
    private String petName;
    private Long ownerId;
    private String ownerName;
    private Long vetId;
    private String vetName;
    private LocalDateTime scheduledAt;
    private String reason;
    private String notes;
    private AppointmentStatus status;
    private LocalDateTime createdAt;
}