package com.PAWPAW.pawpaw.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetReportResponse {

    // ── الشكل القديم (الفرونت الحالي) ──────────────────────────────────────
    private PetDataDto petData;
    private OwnerDataDto ownerData;
    private String aiSummary;
    private List<VitaminDto> vitamins;

    // ── الشكل الجديد (طلب منة) ──────────────────────────────────────────────
    private String petName;
    private String reportDate;
    private List<String> symptoms;
    private String prediction;
    private Integer confidenceScore;
    private List<String> recommendations;

    // ── Nested DTOs ──────────────────────────────────────────────────────────
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PetDataDto {
        private String name;
        private String age;
        private String breed;
        private String weight;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OwnerDataDto {
        private String name;
        private String id;
        private String contact;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VitaminDto {
        private String id;
        private String label;
        private String dose;
        private boolean selected;
    }
}