package com.PAWPAW.pawpaw.admin.service;

import com.PAWPAW.pawpaw.admin.dto.DashboardStats;
import com.PAWPAW.pawpaw.admin.dto.UserSummary;
import com.PAWPAW.pawpaw.appointment.repository.AppointmentRepository;
import com.PAWPAW.pawpaw.auth.entity.User;
import com.PAWPAW.pawpaw.auth.entity.UserRole;
import com.PAWPAW.pawpaw.auth.repository.UserRepository;
import com.PAWPAW.pawpaw.community.repository.PostRepository;
import com.PAWPAW.pawpaw.pet.repository.PetRepository;
import com.PAWPAW.pawpaw.vet.entity.VetProfile;
import com.PAWPAW.pawpaw.vet.repository.VetProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
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

    private UserSummary mapToUserSummary(User user) {
        UserSummary summary = new UserSummary();
        summary.setId(user.getId());
        summary.setFullName(user.getFullName());
        summary.setEmail(user.getEmail());
        summary.setRole(user.getRole());
        summary.setVerified(user.isVerified());
        summary.setCreatedAt(user.getCreatedAt());
        return summary;
    }
}