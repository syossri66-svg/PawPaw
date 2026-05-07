package com.PAWPAW.pawpaw.vet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VetDashboardResponse {
    private VetProfileResponse profile;
    private long totalAppointments;
    private long pendingAppointments;
    private double averageRating;
    private String accountStatus;
    private long consultationsComplete;
    private long newPatientsThisMonth;
    private long aiDiagnosisTimes;
}