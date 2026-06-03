package com.PAWPAW.pawpaw.medical.dto;

import lombok.Data;
import java.util.List;

@Data
public class PetMedicalFullResponse {

    private Long petId;
    private String petName;
    private String petSpecies;
    private String petBreed;
    private String petGender;
    private Double petWeight;
    private String petPhotoUrl;

    // Medical History
    private List<MedicalRecordResponse> medicalHistory;
}