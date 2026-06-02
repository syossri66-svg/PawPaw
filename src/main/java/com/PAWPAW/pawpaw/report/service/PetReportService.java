package com.PAWPAW.pawpaw.report.service;

import com.PAWPAW.pawpaw.report.dto.PetReportResponse;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PetReportService {

    public PetReportResponse generatePetReport(Long petId) {
        // 💡 حالياً بنبني الداتا الـ Mock المطلوبة بالملّي عشان الفرونت إند يربط شاشته فوراً
        PetReportResponse.PetDataDto petData = PetReportResponse.PetDataDto.builder()
                .name("Leo")
                .age("Two years")
                .breed("Golden Retriever")
                .weight("3.2 kg")
                .build();

        PetReportResponse.OwnerDataDto ownerData = PetReportResponse.OwnerDataDto.builder()
                .name("Mooka")
                .id("322223529")
                .contact("201033105182")
                .build();

        List<PetReportResponse.VitaminDto> vitamins = List.of(
                PetReportResponse.VitaminDto.builder().id("b12").label("B12").dose("1 times/day").selected(true).build(),
                PetReportResponse.VitaminDto.builder().id("supp").label("Supplement").dose("4 times/day").selected(true).build()
        );

        return PetReportResponse.builder()
                .petData(petData)
                .ownerData(ownerData)
                .aiSummary("Your dog is fully healthy but we recommend some vitamins cause your dog's weight is 3.2 kg...")
                .vitamins(vitamins)
                .build();
    }
}