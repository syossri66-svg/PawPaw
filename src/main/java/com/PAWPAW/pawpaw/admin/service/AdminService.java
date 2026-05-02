package com.PAWPAW.pawpaw.admin.service;

import com.PAWPAW.pawpaw.admin.dto.DashboardStats;
import com.PAWPAW.pawpaw.admin.dto.FlaggedPostResponse;
import com.PAWPAW.pawpaw.admin.dto.SystemStats;
import com.PAWPAW.pawpaw.admin.dto.UserSummary;
import com.PAWPAW.pawpaw.appointment.repository.AppointmentRepository;
import com.PAWPAW.pawpaw.auth.entity.User;
import com.PAWPAW.pawpaw.auth.entity.UserRole;
import com.PAWPAW.pawpaw.auth.repository.UserRepository;
import com.PAWPAW.pawpaw.community.repository.PostRepository;
import com.PAWPAW.pawpaw.pet.repository.PetRepository;
import com.PAWPAW.pawpaw.vet.dto.VetProfileResponse;
import com.PAWPAW.pawpaw.vet.entity.VetProfile;
import com.PAWPAW.pawpaw.vet.repository.VetProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final PetRepository petRepository;
    private final AppointmentRepository appointmentRepository;
    private final PostRepository postRepository;
    private final VetProfileRepository vetProfileRepository;

    public DashboardStats getDashboardStats() {
        DashboardStats stats = new DashboardStats();
        stats.setTotalUsers(userRepository.count());
        stats.setTotalVets(userRepository.countByRole(UserRole.ROLE_VET));
        stats.setTotalPetOwners(userRepository.countByRole(UserRole.ROLE_PET_OWNER));
        stats.setTotalPets(petRepository.count());
        stats.setTotalAppointments(appointmentRepository.count());
        stats.setTotalPosts(postRepository.count());
        stats.setVerifiedVets(vetProfileRepository.countByIsApprovedTrue());
        stats.setPendingVets(vetProfileRepository.countByIsApprovedFalse());
        return stats;
    }

    public List<UserSummary> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToUserSummary)
                .collect(Collectors.toList());
    }

    public List<UserSummary> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role)
                .stream()
                .map(this::mapToUserSummary)
                .collect(Collectors.toList());
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public UserSummary banUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setBanned(true);
        return mapToUserSummary(userRepository.save(user));
    }

    public UserSummary approveVet(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        vetProfileRepository.findByCustomUserId(userId).ifPresent(profile -> {
            profile.setApproved(true);
            vetProfileRepository.save(profile);
        });
        return mapToUserSummary(user);
    }

    public UserSummary rejectVet(Long userId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        vetProfileRepository.findByCustomUserId(userId).ifPresent(profile -> {
            profile.setApproved(false);
            profile.setRejectionReason(reason);
            vetProfileRepository.save(profile);
        });
        return mapToUserSummary(user);
    }

    public List<VetProfileResponse> getPendingVets() {
        return vetProfileRepository.findByIsApprovedFalse()
                .stream()
                .map(this::mapToVetResponse)
                .collect(Collectors.toList());
    }

    public List<VetProfileResponse> searchVets(String keyword) {
        return vetProfileRepository.searchVets(keyword)
                .stream()
                .map(this::mapToVetResponse)
                .collect(Collectors.toList());
    }

    public Map<String, Long> getVetStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("pending", vetProfileRepository.countByIsApprovedFalse());
        stats.put("verified", vetProfileRepository.countByIsApprovedTrue());
        return stats;
    }

    private UserSummary mapToUserSummary(User user) {
        UserSummary summary = new UserSummary();
        summary.setId(user.getId());
        summary.setFullName(user.getFullName());
        summary.setEmail(user.getEmail());
        summary.setRole(user.getRole());
        summary.setVerified(user.isVerified());
        summary.setBanned(user.isBanned());
        summary.setCreatedAt(user.getCreatedAt());
        return summary;
    }

    private VetProfileResponse mapToVetResponse(VetProfile profile) {
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
    public UserSummary unbanUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setBanned(false);
        return mapToUserSummary(userRepository.save(user));
    }

    public List<UserSummary> searchUsers(String keyword) {
        return userRepository.searchUsers(keyword)
                .stream()
                .map(this::mapToUserSummary)
                .collect(Collectors.toList());
    }
    public List<FlaggedPostResponse> getAllPosts() {
        return postRepository.findAll()
                .stream()
                .map(this::mapToFlaggedPost)
                .collect(Collectors.toList());
    }

    public void deletePost(Long postId) {
        postRepository.deleteById(postId);
    }

    private FlaggedPostResponse mapToFlaggedPost(com.PAWPAW.pawpaw.community.entity.Post post) {
        FlaggedPostResponse response = new FlaggedPostResponse();
        response.setId(post.getId());
        response.setContent(post.getContent());
        response.setImageUrl(post.getImageUrl());
        response.setUserId(post.getUser().getId());
        response.setUserName(post.getUser().getFullName());
        response.setCreatedAt(post.getCreatedAt());
        return response;
    }
    public SystemStats getSystemStats() {
        SystemStats stats = new SystemStats();
        stats.setTotalUsers(userRepository.count());
        stats.setTotalVets(userRepository.countByRole(UserRole.ROLE_VET));
        stats.setTotalPetOwners(userRepository.countByRole(UserRole.ROLE_PET_OWNER));
        stats.setTotalPets(petRepository.count());
        stats.setTotalAppointments(appointmentRepository.count());
        stats.setTotalPosts(postRepository.count());
        stats.setStatus("UP");
        return stats;
    }
    public List<Map<String, Object>> getActivityLogs() {
        return postRepository.findAll()
                .stream()
                .map(post -> {
                    Map<String, Object> log = new HashMap<>();
                    log.put("timestamp", post.getCreatedAt());
                    log.put("adminName", "System");
                    log.put("actionType", "POST_CREATED");
                    log.put("description", "Post by " + post.getUser().getFullName());
                    log.put("ipAddress", "0.0.0.0");
                    return log;
                })
                .collect(Collectors.toList());
    }

    public void warnUserByPost(Long postId) {
        postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

    }
}