package com.PAWPAW.pawpaw.medical.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MedicalRecordRequest {

    @NotNull
    private Long petId;
    private List<String> allergies;
    private String prescription;
    private String dosage;
    private String duration;
    private String reportUrl;

    private String diagnosis;
    private String treatment;
    private String notes;
    private String clinicName;
    private Double weight;
    private LocalDateTime visitDate;

    private List<MedicationRequest> medications;
    private String vaccinationStatus;
    private LocalDate nextVaccinationDate;
    private LocalDate nextVisitDate;
    private String rxNumber;
    private String clinicalNotes;
    private Boolean hasAiReport;
    private String visitTitle;
}