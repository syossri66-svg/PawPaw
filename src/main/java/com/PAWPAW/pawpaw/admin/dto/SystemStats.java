package com.PAWPAW.pawpaw.admin.dto;

import lombok.Data;

@Data
public class SystemStats {
    private long totalUsers;
    private long totalVets;
    private long totalPetOwners;
    private long totalPets;
    private long totalAppointments;
    private long totalPosts;
    private long totalMessages;
    private long totalGroups;
    private String status;
}