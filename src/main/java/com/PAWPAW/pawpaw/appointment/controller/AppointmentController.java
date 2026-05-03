package com.PAWPAW.pawpaw.appointment.controller;

import com.PAWPAW.pawpaw.appointment.dto.AppointmentRequest;
import com.PAWPAW.pawpaw.appointment.dto.AppointmentResponse;
import com.PAWPAW.pawpaw.appointment.entity.AppointmentStatus;
import com.PAWPAW.pawpaw.appointment.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<AppointmentResponse> bookAppointment(
            @Valid @RequestBody AppointmentRequest request) {
        return ResponseEntity.ok(appointmentService.bookAppointment(request));
    }

    @GetMapping("/my")
    public ResponseEntity<List<AppointmentResponse>> getMyAppointments() {
        return ResponseEntity.ok(appointmentService.getMyAppointments());
    }

    @GetMapping("/vet")
    public ResponseEntity<List<AppointmentResponse>> getVetAppointments() {
        return ResponseEntity.ok(appointmentService.getVetAppointments());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<AppointmentResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam AppointmentStatus status) {
        return ResponseEntity.ok(appointmentService.updateStatus(id, status));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelAppointment(@PathVariable Long id) {
        appointmentService.cancelAppointment(id);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/{id}/confirm")
    public ResponseEntity<AppointmentResponse> confirmAppointment(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.updateStatus(id, AppointmentStatus.CONFIRMED));
    }
}