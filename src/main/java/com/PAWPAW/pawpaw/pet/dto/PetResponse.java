package com.PAWPAW.pawpaw.pet.dto;

import com.PAWPAW.pawpaw.pet.entity.PetGender;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PetResponse {
    private Long id;
    private String name;
    private String species;
    private String breed;
    private LocalDate dateOfBirth;
    private String photoUrl;
    private String medicalNotes;
    private PetGender gender;
    private Long ownerId;
    private String ownerName;
    private LocalDateTime createdAt;
    private String vetName;
    private String lastDiagnosis;
    private LocalDateTime lastVisitDate;
    private String healthStatus;
    private String uniqueId;
    private Boolean vaccinated;
    private Double weight;
}