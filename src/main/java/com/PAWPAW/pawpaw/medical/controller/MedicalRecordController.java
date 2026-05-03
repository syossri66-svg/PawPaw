package com.PAWPAW.pawpaw.medical.controller;

import com.PAWPAW.pawpaw.medical.dto.MedicalRecordRequest;
import com.PAWPAW.pawpaw.medical.dto.MedicalRecordResponse;
import com.PAWPAW.pawpaw.medical.service.MedicalRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/medical")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    @PostMapping
    public ResponseEntity<MedicalRecordResponse> addRecord(
            @Valid @RequestBody MedicalRecordRequest request) {
        return ResponseEntity.ok(medicalRecordService.addRecord(request));
    }

    @GetMapping("/pet/{petId}")
    public ResponseEntity<List<MedicalRecordResponse>> getPetRecords(
            @PathVariable Long petId) {
        return ResponseEntity.ok(medicalRecordService.getPetRecords(petId));
    }

    @GetMapping("/vet")
    public ResponseEntity<List<MedicalRecordResponse>> getMyVetRecords() {
        return ResponseEntity.ok(medicalRecordService.getMyVetRecords());
    }
}