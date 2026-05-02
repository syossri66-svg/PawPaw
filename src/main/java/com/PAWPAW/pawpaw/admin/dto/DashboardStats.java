package com.PAWPAW.pawpaw.admin.dto;

import lombok.Data;

@Data
public class DashboardStats {
    private long totalUsers;
    private long totalVets;
    private long totalPetOwners;
    private long totalPets;
    private long totalAppointments;
    private long totalPosts;
    private long verifiedVets;
    private long pendingVets;
}