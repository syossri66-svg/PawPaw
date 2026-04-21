package com.PAWPAW.pawpaw.vet.dto;

import lombok.Data;

@Data
public class VetProfileResponse {
    private Long id;
    private Long userId;
    private String vetName;
    private String clinicName;
    private String clinicAddress;
    private String phoneNumber;
    private String specialization;
    private String licenseNumber;
    private boolean isApproved;
    private String bio;
    private Double latitude;
    private Double longitude;
}