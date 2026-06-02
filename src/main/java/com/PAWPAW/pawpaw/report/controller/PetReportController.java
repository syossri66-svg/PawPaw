package com.PAWPAW.pawpaw.report.controller;

import com.PAWPAW.pawpaw.report.dto.PetReportResponse;
import com.PAWPAW.pawpaw.report.service.PetReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pet-report")
@RequiredArgsConstructor
public class PetReportController {

    private final PetReportService petReportService;


    @GetMapping("/{id}")
    public ResponseEntity<PetReportResponse> getPetReport(@PathVariable("id") Long petId) {
        PetReportResponse report = petReportService.generatePetReport(petId);
        return ResponseEntity.ok(report);
    }
}