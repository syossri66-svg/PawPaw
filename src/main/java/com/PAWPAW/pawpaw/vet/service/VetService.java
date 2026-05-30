package com.PAWPAW.pawpaw.vet.service;

import com.PAWPAW.pawpaw.appointment.entity.AppointmentStatus;
import com.PAWPAW.pawpaw.appointment.repository.AppointmentRepository;
import com.PAWPAW.pawpaw.auth.entity.User;
import com.PAWPAW.pawpaw.auth.repository.UserRepository;
import com.PAWPAW.pawpaw.medical.repository.MedicalRecordRepository;
import com.PAWPAW.pawpaw.vet.dto.VetDashboardResponse;
import com.PAWPAW.pawpaw.vet.dto.VetProfileRequest;
import com.PAWPAW.pawpaw.vet.dto.VetProfileResponse;
import com.PAWPAW.pawpaw.vet.entity.VetProfile;
import com.PAWPAW.pawpaw.vet.repository.VetProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VetService {
    private final MedicalRecordRepository medicalRecordRepository;

    private final VetProfileRepository vetProfileRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;


    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public VetProfileResponse createOrUpdateProfile(VetProfileRequest request) {
        User user = getCurrentUser();



        VetProfile profile = vetProfileRepository.findByCustomUserId(user.getId())
                .orElse(new VetProfile());

        profile.setUser(user);
        profile.setClinicName(request.getClinicName());
        profile.setClinicAddress(request.getClinicAddress());
        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setSpecialization(request.getSpecialization());
        profile.setLicenseNumber(request.getLicenseNumber());
        profile.setBio(request.getBio());
        profile.setLatitude(request.getLatitude());
        profile.setLongitude(request.getLongitude());
        profile.setYearsOfExperience(request.getYearsOfExperience());

        return mapToResponse(vetProfileRepository.save(profile));
    }

    public VetProfileResponse getMyProfile() {
        User user = getCurrentUser();


        VetProfile profile = vetProfileRepository.findByCustomUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        return mapToResponse(profile);
    }

    public List<VetProfileResponse> getAllApprovedVets() {
        return vetProfileRepository.findByIsApprovedTrue()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<VetProfileResponse> searchVetsBySpecialization(String specialization) {
        return vetProfileRepository.findBySpecializationContainingIgnoreCase(specialization)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private VetProfileResponse mapToResponse(VetProfile profile) {
        VetProfileResponse response = new VetProfileResponse();
        response.setId(profile.getId());
        response.setUserId(profile.getUser().getId());
        response.setVetName(profile.getUser().getFullName());
        response.setClinicName(profile.getClinicName());
        response.setClinicAddress(profile.getClinicAddress());
        response.setPhoneNumber(profile.getPhoneNumber());
        response.setSpecialization(profile.getSpecialization());
        response.setLicenseNumber(profile.getLicenseNumber());
        response.setApproved(profile.isApproved());
        response.setBio(profile.getBio());
        response.setLatitude(profile.getLatitude());
        response.setLongitude(profile.getLongitude());
        response.setYearsOfExperience(profile.getYearsOfExperience());
        return response;
    }
    public VetDashboardResponse getMyDashboard() {
        User user = getCurrentUser();
        VetProfile profile = vetProfileRepository.findByCustomUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        Long vetId = user.getId();
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        long totalAppointments = appointmentRepository.countByVetId(vetId);
        long pendingAppointments = appointmentRepository.countByVetIdAndStatus(vetId, AppointmentStatus.PENDING);
        long completedAppointments = appointmentRepository.countByVetIdAndStatus(vetId, AppointmentStatus.COMPLETED);
        long newPatients = appointmentRepository.countNewPatientsThisMonth(vetId, startOfMonth);
        long aiDiagnoses = medicalRecordRepository.findByVetId(vetId)
                .stream().filter(r -> Boolean.TRUE.equals(r.getHasAiReport())).count();

        // Upcoming
        List<VetDashboardResponse.UpcomingAppointment> upcoming = appointmentRepository
                .findByVetIdAndStatusOrderByScheduledAtAsc(vetId, AppointmentStatus.PENDING)
                .stream().limit(5).map(a -> {
                    VetDashboardResponse.UpcomingAppointment ua = new VetDashboardResponse.UpcomingAppointment();
                    ua.setId(a.getId());
                    ua.setPetName(a.getPet().getName());
                    ua.setBreed(a.getPet().getBreed());
                    ua.setTime(a.getScheduledAt().format(formatter));
                    ua.setAvatarUrl(a.getPet().getPhotoUrl());
                    return ua;
                }).collect(Collectors.toList());

        // Recent Cases
        List<VetDashboardResponse.RecentCase> recentCases = medicalRecordRepository
                .findByVetId(vetId).stream().limit(5).map(r -> {
                    VetDashboardResponse.RecentCase rc = new VetDashboardResponse.RecentCase();
                    rc.setCaseId(r.getId());
                    rc.setPetName(r.getPet().getName());
                    rc.setOwnerName(r.getPet().getOwner().getFullName());
                    rc.setStatus(r.getPet().getHealthStatus() != null ? r.getPet().getHealthStatus() : "Stable");
                    rc.setImageUrl(r.getPet().getPhotoUrl());
                    return rc;
                }).collect(Collectors.toList());

        return VetDashboardResponse.builder()
                .profile(mapToResponse(profile))
                .totalAppointments(totalAppointments)
                .pendingAppointments(pendingAppointments)
                .averageRating(0.0)
                .accountStatus(profile.isApproved() ? "APPROVED" : "PENDING")
                .consultationsComplete(completedAppointments)
                .newPatientsThisMonth(newPatients)
                .aiDiagnosisTimes(aiDiagnoses)
                .upcomingAppointments(upcoming)
                .recentCases(recentCases)
                .build();
    }


}