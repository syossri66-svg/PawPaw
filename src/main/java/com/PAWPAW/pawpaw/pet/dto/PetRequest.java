package com.PAWPAW.pawpaw.pet.dto;

import com.PAWPAW.pawpaw.pet.entity.PetGender;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDate;

@Data
public class PetRequest {

    @NotBlank
    private String name;

    private String species;

    private String breed;

    private LocalDate dateOfBirth;

    private String photoUrl;

    private String medicalNotes;

    private PetGender gender;
}