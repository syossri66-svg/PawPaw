package com.PAWPAW.pawpaw.medical.dto;

import lombok.Data;

@Data
public class MedicationResponse {
    private Long id;
    private String name;
    private String strength;
    private String instruction;
    private String duration;
}