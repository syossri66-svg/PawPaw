package com.PAWPAW.pawpaw.medical.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MedicalRecordTimelineResponse {
    private Long id;
    private String visitTitle;
    private String clinicName;
    private LocalDateTime visitDate;
}