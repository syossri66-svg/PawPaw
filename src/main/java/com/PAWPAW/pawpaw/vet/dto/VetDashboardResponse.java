package com.PAWPAW.pawpaw.vet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

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
    private List<UpcomingAppointment> upcomingAppointments;
    private List<RecentCase> recentCases;

    @Data
    public static class UpcomingAppointment {
        private Long id;
        private String petName;
        private String breed;
        private String time;
        private String avatarUrl;
    }

    @Data
    public static class RecentCase {
        private Long caseId;
        private String petName;
        private String ownerName;
        private String status;
        private String imageUrl;
    }
}