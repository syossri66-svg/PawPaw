package com.PAWPAW.pawpaw.appointment.service;

import com.PAWPAW.pawpaw.appointment.dto.AppointmentRequest;
import com.PAWPAW.pawpaw.appointment.dto.AppointmentResponse;
import com.PAWPAW.pawpaw.appointment.entity.Appointment;
import com.PAWPAW.pawpaw.appointment.entity.AppointmentStatus;
import com.PAWPAW.pawpaw.appointment.repository.AppointmentRepository;
import com.PAWPAW.pawpaw.auth.entity.User;
import com.PAWPAW.pawpaw.auth.repository.UserRepository;
import com.PAWPAW.pawpaw.pet.entity.Pet;
import com.PAWPAW.pawpaw.pet.repository.PetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final PetRepository petRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public AppointmentResponse bookAppointment(AppointmentRequest request) {
        User owner = getCurrentUser();

        Pet pet = petRepository.findById(request.getPetId())
                .orElseThrow(() -> new RuntimeException("Pet not found"));

        User vet = userRepository.findById(request.getVetId())
                .orElseThrow(() -> new RuntimeException("Vet not found"));

        Appointment appointment = Appointment.builder()
                .pet(pet)
                .owner(owner)
                .vet(vet)
                .scheduledAt(request.getScheduledAt())
                .reason(request.getReason())
                .build();

        return mapToResponse(appointmentRepository.save(appointment));
    }

    public List<AppointmentResponse> getMyAppointments() {
        User user = getCurrentUser();
        return appointmentRepository.findByOwnerId(user.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<AppointmentResponse> getVetAppointments() {
        User vet = getCurrentUser();
        return appointmentRepository.findByVetId(vet.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public AppointmentResponse updateStatus(Long id, AppointmentStatus status) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        appointment.setStatus(status);
        return mapToResponse(appointmentRepository.save(appointment));
    }

    public void cancelAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
    }

    private AppointmentResponse mapToResponse(Appointment a) {
        AppointmentResponse response = new AppointmentResponse();
        response.setId(a.getId());
        response.setPetId(a.getPet().getId());
        response.setPetName(a.getPet().getName());
        response.setOwnerId(a.getOwner().getId());
        response.setOwnerName(a.getOwner().getFullName());
        response.setVetId(a.getVet().getId());
        response.setVetName(a.getVet().getFullName());
        response.setScheduledAt(a.getScheduledAt());
        response.setReason(a.getReason());
        response.setNotes(a.getNotes());
        response.setStatus(a.getStatus());
        response.setCreatedAt(a.getCreatedAt());
        return response;
    }
}