package com.PAWPAW.pawpaw.vet.controller;

import com.PAWPAW.pawpaw.vet.dto.VetDashboardResponse;
import com.PAWPAW.pawpaw.vet.dto.VetProfileRequest;
import com.PAWPAW.pawpaw.vet.dto.VetProfileResponse;
import com.PAWPAW.pawpaw.vet.service.VetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vets")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VetController {

    private final VetService vetService;

    @PostMapping("/profile")
    public ResponseEntity<VetProfileResponse> createOrUpdateProfile(
            @Valid @RequestBody VetProfileRequest request) {
        return ResponseEntity.ok(vetService.createOrUpdateProfile(request));
    }

    @GetMapping("/profile")
    public ResponseEntity<VetProfileResponse> getMyProfile() {
        return ResponseEntity.ok(vetService.getMyProfile());
    }

    @GetMapping
    public ResponseEntity<List<VetProfileResponse>> getAllApprovedVets() {
        return ResponseEntity.ok(vetService.getAllApprovedVets());
    }

    @GetMapping("/search")
    public ResponseEntity<List<VetProfileResponse>> searchVets(
            @RequestParam String specialization) {
        return ResponseEntity.ok(vetService.searchVetsBySpecialization(specialization));
    }
    @GetMapping("/dashboard")
    public ResponseEntity<VetDashboardResponse> getMyDashboard() {
        return ResponseEntity.ok(vetService.getMyDashboard());
    }



}