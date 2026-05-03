package com.PAWPAW.pawpaw.medical.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MedicalRecordRequest {

    @NotNull
    private Long petId;

    private String diagnosis;
    private String treatment;
    private String notes;
    private String clinicName;
    private Double weight;
    private LocalDateTime visitDate;
}