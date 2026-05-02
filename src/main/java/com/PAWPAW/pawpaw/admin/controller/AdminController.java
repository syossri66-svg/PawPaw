package com.PAWPAW.pawpaw.admin.controller;

import com.PAWPAW.pawpaw.admin.dto.DashboardStats;
import com.PAWPAW.pawpaw.admin.dto.FlaggedPostResponse;
import com.PAWPAW.pawpaw.admin.dto.SystemStats;
import com.PAWPAW.pawpaw.admin.dto.UserSummary;
import com.PAWPAW.pawpaw.admin.service.AdminService;
import com.PAWPAW.pawpaw.auth.entity.UserRole;
import com.PAWPAW.pawpaw.vet.dto.VetProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStats> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserSummary>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/users/role/{role}")
    public ResponseEntity<List<UserSummary>> getUsersByRole(@PathVariable UserRole role) {
        return ResponseEntity.ok(adminService.getUsersByRole(role));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/users/{id}/ban")
    public ResponseEntity<UserSummary> banUser(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.banUser(id));
    }

    @PutMapping("/vets/{userId}/approve")
    public ResponseEntity<UserSummary> approveVet(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.approveVet(userId));
    }

    @PutMapping("/vets/{userId}/reject")
    public ResponseEntity<UserSummary> rejectVet(@PathVariable Long userId,
                                                 @RequestParam String reason) {
        return ResponseEntity.ok(adminService.rejectVet(userId, reason));
    }

    @GetMapping("/vets/pending")
    public ResponseEntity<List<VetProfileResponse>> getPendingVets() {
        return ResponseEntity.ok(adminService.getPendingVets());
    }

    @GetMapping("/vets/search")
    public ResponseEntity<List<VetProfileResponse>> searchVets(@RequestParam String keyword) {
        return ResponseEntity.ok(adminService.searchVets(keyword));
    }

    @GetMapping("/vets/stats")
    public ResponseEntity<Map<String, Long>> getVetStats() {
        return ResponseEntity.ok(adminService.getVetStats());
    }
    @PutMapping("/users/{id}/unban")
    public ResponseEntity<UserSummary> unbanUser(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.unbanUser(id));
    }

    @GetMapping("/users/search")
    public ResponseEntity<List<UserSummary>> searchUsers(@RequestParam String keyword) {
        return ResponseEntity.ok(adminService.searchUsers(keyword));
    }
    @GetMapping("/posts")
    public ResponseEntity<List<FlaggedPostResponse>> getAllPosts() {
        return ResponseEntity.ok(adminService.getAllPosts());
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        adminService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/system/stats")
    public ResponseEntity<SystemStats> getSystemStats() {
        return ResponseEntity.ok(adminService.getSystemStats());
    }

    @GetMapping("/system/health")
    public ResponseEntity<?> getSystemHealth() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("database", "Connected");
        health.put("version", "1.0.0");
        return ResponseEntity.ok(health);
    }
    @GetMapping("/system/settings")
    public ResponseEntity<Map<String, Object>> getSystemSettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("maintenanceMode", false);
        settings.put("aiModerationLevel", "Medium");
        settings.put("emailNotifications", true);
        return ResponseEntity.ok(settings);
    }

    @PutMapping("/system/settings")
    public ResponseEntity<Map<String, Object>> updateSystemSettings(
            @RequestBody Map<String, Object> settings) {
        return ResponseEntity.ok(settings);
    }

    @GetMapping("/system/logs")
    public ResponseEntity<List<Map<String, Object>>> getActivityLogs() {
        return ResponseEntity.ok(adminService.getActivityLogs());
    }

    @PutMapping("/posts/{id}/warn")
    public ResponseEntity<Void> warnUser(@PathVariable Long id) {
        adminService.warnUserByPost(id);
        return ResponseEntity.ok().build();
    }
}