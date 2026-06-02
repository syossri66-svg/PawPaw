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
    private PetDataDto petData;
    private OwnerDataDto ownerData;
    private String aiSummary;
    private List<VitaminDto> vitamins;

    @Data
    @Builder
    public static class PetDataDto {
        private String name;
        private String age;
        private String breed;
        private String weight;
    }

    @Data
    @Builder
    public static class OwnerDataDto {
        private String name;
        private String id;
        private String contact;
    }

    @Data
    @Builder
    public static class VitaminDto {
        private String id;
        private String label;
        private String dose;
        private boolean selected;
    }
}