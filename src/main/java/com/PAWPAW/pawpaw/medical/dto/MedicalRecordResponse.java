package com.PAWPAW.pawpaw.medical.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MedicalRecordResponse {
    private Long id;
    private Long petId;
    private String petName;
    private Long vetId;
    private String vetName;
    private String diagnosis;
    private String treatment;
    private String notes;
    private String clinicName;
    private Double weight;
    private LocalDateTime visitDate;
    private LocalDateTime createdAt;
}