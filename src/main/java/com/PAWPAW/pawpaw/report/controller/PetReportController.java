package com.PAWPAW.pawpaw.report.controller;

import com.PAWPAW.pawpaw.report.dto.PetReportResponse;
import com.PAWPAW.pawpaw.report.service.PetReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;

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



    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "error", "Bad Request",
                "message", "The provided ID format is invalid. Expected a numeric value (Long)."
        ));
    }
}