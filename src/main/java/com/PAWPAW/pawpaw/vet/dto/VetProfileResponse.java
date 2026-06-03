package com.PAWPAW.pawpaw.vet.dto;

import lombok.Data;
import java.util.List;

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
    private Integer yearsOfExperience;
    private long totalPatients;


    private List<CertificationDto> certifications;
    private List<AppointmentDto> appointments;

    @Data
    public static class AppointmentDto {
        private Long id;
        private String petName;
        private String date;
        private String status;
    }
}