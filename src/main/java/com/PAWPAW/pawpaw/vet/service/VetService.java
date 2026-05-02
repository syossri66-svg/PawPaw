package com.PAWPAW.pawpaw.vet.service;

import com.PAWPAW.pawpaw.auth.entity.User;
import com.PAWPAW.pawpaw.auth.repository.UserRepository;
import com.PAWPAW.pawpaw.vet.dto.VetDashboardResponse;
import com.PAWPAW.pawpaw.vet.dto.VetProfileRequest;
import com.PAWPAW.pawpaw.vet.dto.VetProfileResponse;
import com.PAWPAW.pawpaw.vet.entity.VetProfile;
import com.PAWPAW.pawpaw.vet.repository.VetProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VetService {

    private final VetProfileRepository vetProfileRepository;
    private final UserRepository userRepository;

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
        return response;
    }
    public VetDashboardResponse getMyDashboard() {
        User user = getCurrentUser();
        VetProfile profile = vetProfileRepository.findByCustomUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        return VetDashboardResponse.builder()
                .profile(mapToResponse(profile))
                .totalAppointments(0L)
                .pendingAppointments(0L)
                .averageRating(0.0)
                .accountStatus(profile.isApproved() ? "APPROVED" : "PENDING")
                .build();
    }


}