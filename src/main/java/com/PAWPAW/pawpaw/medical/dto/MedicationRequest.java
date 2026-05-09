package com.PAWPAW.pawpaw.medical.dto;

import lombok.Data;

@Data
public class MedicationRequest {
    private String name;
    private String strength;
    private String instruction;
    private String duration;
}