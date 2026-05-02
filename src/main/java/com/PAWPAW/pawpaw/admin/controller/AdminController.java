package com.PAWPAW.pawpaw.admin.controller;

import com.PAWPAW.pawpaw.admin.dto.DashboardStats;
import com.PAWPAW.pawpaw.admin.dto.UserSummary;
import com.PAWPAW.pawpaw.admin.service.AdminService;
import com.PAWPAW.pawpaw.auth.entity.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}