package com.PAWPAW.pawpaw.vet.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VetProfileRequest {

    @NotBlank
    private String clinicName;

    @NotBlank
    private String clinicAddress;

    private String phoneNumber;

    private String specialization;

    @NotBlank
    private String licenseNumber;

    private String bio;

    private Double latitude;

    private Double longitude;
}