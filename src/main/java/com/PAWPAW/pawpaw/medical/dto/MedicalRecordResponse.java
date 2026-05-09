package com.PAWPAW.pawpaw.medical.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

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
    private List<String> allergies;
    private String prescription;
    private String dosage;
    private String duration;
    private String reportUrl;
    // Pet snapshot
    private String petPhotoUrl;
    private String petSpecies;
    private String petBreed;
    private Double petWeight;
}