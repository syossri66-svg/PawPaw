package com.PAWPAW.pawpaw.vet.service;

import com.PAWPAW.pawpaw.appointment.entity.AppointmentStatus;
import com.PAWPAW.pawpaw.appointment.repository.AppointmentRepository;
import com.PAWPAW.pawpaw.auth.entity.User;
import com.PAWPAW.pawpaw.auth.repository.UserRepository;
import com.PAWPAW.pawpaw.medical.repository.MedicalRecordRepository;
import com.PAWPAW.pawpaw.vet.dto.CertificationDto;
import com.PAWPAW.pawpaw.vet.dto.VetDashboardResponse;
import com.PAWPAW.pawpaw.vet.dto.VetProfileRequest;
import com.PAWPAW.pawpaw.vet.dto.VetProfileResponse;
import com.PAWPAW.pawpaw.vet.entity.Certification;
import com.PAWPAW.pawpaw.vet.entity.VetProfile;
import com.PAWPAW.pawpaw.vet.repository.CertificationRepository;
import com.PAWPAW.pawpaw.vet.repository.VetProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VetService {
    private final MedicalRecordRepository medicalRecordRepository;
    private final VetProfileRepository vetProfileRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final CertificationRepository certificationRepository;

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

        VetProfile saved = vetProfileRepository.save(profile);
        saveCertifications(saved, request.getCertifications());
        return mapToResponse(saved);
    }

    public VetProfileResponse getMyProfile() {
        User user = getCurrentUser();
        VetProfile profile = vetProfileRepository.findByCustomUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Profile not found"));
        return mapToResponse(profile);
    }

    // ✅ GET /api/vets/{id}
    public VetProfileResponse getVetById(Long id) {
        VetProfile profile = vetProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vet not found"));
        return mapToResponse(profile);
    }

    // ✅ PATCH /api/vets/{id}
    @Transactional
    public VetProfileResponse updateVetById(Long id, VetProfileRequest request) {
        VetProfile profile = vetProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vet not found"));

        if (request.getClinicName() != null) profile.setClinicName(request.getClinicName());
        if (request.getClinicAddress() != null) profile.setClinicAddress(request.getClinicAddress());
        if (request.getPhoneNumber() != null) profile.setPhoneNumber(request.getPhoneNumber());
        if (request.getSpecialization() != null) profile.setSpecialization(request.getSpecialization());
        if (request.getLicenseNumber() != null) profile.setLicenseNumber(request.getLicenseNumber());
        if (request.getBio() != null) profile.setBio(request.getBio());
        if (request.getLatitude() != null) profile.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) profile.setLongitude(request.getLongitude());
        if (request.getYearsOfExperience() != null) profile.setYearsOfExperience(request.getYearsOfExperience());

        VetProfile saved = vetProfileRepository.save(profile);
        saveCertifications(saved, request.getCertifications());
        return mapToResponse(saved);
    }

    // ✅ POST /api/vets/{id}/certificate
    public CertificationDto addCertificate(Long vetId, CertificationDto dto) {
        VetProfile profile = vetProfileRepository.findById(vetId)
                .orElseThrow(() -> new RuntimeException("Vet not found"));

        Certification cert = Certification.builder()
                .vetProfile(profile)
                .title(dto.getTitle())
                .status(dto.getStatus() != null ? dto.getStatus() : "PENDING")
                .imageUrl(dto.getImageUrl())
                .build();

        Certification saved = certificationRepository.save(cert);
        CertificationDto result = new CertificationDto();
        result.setId(saved.getId());
        result.setTitle(saved.getTitle());
        result.setStatus(saved.getStatus());
        result.setImageUrl(saved.getImageUrl());
        return result;
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

    @Transactional
    private void saveCertifications(VetProfile profile, List<CertificationDto> certDtos) {
        if (certDtos == null || certDtos.isEmpty()) return;
        certificationRepository.deleteByVetProfileId(profile.getId());
        for (CertificationDto dto : certDtos) {
            Certification cert = Certification.builder()
                    .vetProfile(profile)
                    .title(dto.getTitle())
                    .status(dto.getStatus() != null ? dto.getStatus() : "PENDING")
                    .imageUrl(dto.getImageUrl())
                    .build();
            certificationRepository.save(cert);
        }
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

        // ✅ Map certifications
        List<CertificationDto> certDtos = certificationRepository
                .findByVetProfileId(profile.getId())
                .stream().map(c -> {
                    CertificationDto dto = new CertificationDto();
                    dto.setId(c.getId());
                    dto.setTitle(c.getTitle());
                    dto.setStatus(c.getStatus());
                    dto.setImageUrl(c.getImageUrl());
                    return dto;
                }).collect(Collectors.toList());
        response.setCertifications(certDtos);

        // ✅ Map appointments
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        List<VetProfileResponse.AppointmentDto> appointments = appointmentRepository
                .findByVetIdAndStatusOrderByScheduledAtAsc(profile.getUser().getId(), AppointmentStatus.PENDING)
                .stream().map(a -> {
                    VetProfileResponse.AppointmentDto dto = new VetProfileResponse.AppointmentDto();
                    dto.setId(a.getId());
                    dto.setPetName(a.getPet().getName());
                    dto.setDate(a.getScheduledAt().format(formatter));
                    dto.setStatus(a.getStatus().name());
                    return dto;
                }).collect(Collectors.toList());
        response.setAppointments(appointments);

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