package com.PAWPAW.pawpaw.auth.controller;

import com.PAWPAW.pawpaw.auth.dto.AuthResponse;
import com.PAWPAW.pawpaw.auth.dto.LoginRequest;
import com.PAWPAW.pawpaw.auth.dto.RegisterRequest;
import com.PAWPAW.pawpaw.auth.dto.UserResponse;
import com.PAWPAW.pawpaw.auth.entity.User;
import com.PAWPAW.pawpaw.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final com.PAWPAW.pawpaw.auth.repository.UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof User) {
            return ResponseEntity.ok(mapToResponse((User) principal));
        }

        return userRepository.findByEmail(authentication.getName())
                .map(user -> ResponseEntity.ok(mapToResponse(user)))
                .orElse(ResponseEntity.status(404).build());
    }

    @GetMapping("/profile/{id}")
    public ResponseEntity<UserResponse> getPublicProfile(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(this::mapToResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateProfileDetails(@RequestBody Map<String, Object> updates) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (updates.containsKey("location")) currentUser.setLocation((String) updates.get("location"));
        if (updates.containsKey("bio")) currentUser.setBio((String) updates.get("bio"));

        userRepository.save(currentUser);
        return ResponseEntity.ok(mapToResponse(currentUser));
    }


    @PatchMapping(value = "/me/avatar", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> updateAvatar(@RequestParam(value = "file", required = false) MultipartFile file,
                                                            @RequestParam(value = "avatar", required = false) MultipartFile alternateFile) throws IOException {

        MultipartFile finalFile = (file != null) ? file : alternateFile;

        if (finalFile == null || finalFile.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
        }

        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String filename = System.currentTimeMillis() + "_" + finalFile.getOriginalFilename();
        String rootDir = System.getProperty("user.dir");

        File uploadDir = new File(rootDir + File.separator + "uploads");
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        File targetFile = new File(uploadDir + File.separator + filename);
        finalFile.transferTo(targetFile);

        String fileUrl = "https://pawpaw-app.up.railway.app/uploads/" + filename;
        currentUser.setAvatarUrl(fileUrl);
        userRepository.save(currentUser);

        return ResponseEntity.ok(Map.of(
                "profilePicture", fileUrl,
                "message", "Avatar updated successfully"
        ));
    }


    @PatchMapping(value = "/me/cover", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> updateCover(@RequestParam(value = "file", required = false) MultipartFile file,
                                                           @RequestParam(value = "cover", required = false) MultipartFile alternateFile) throws IOException {

        MultipartFile finalFile = (file != null) ? file : alternateFile;

        if (finalFile == null || finalFile.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
        }

        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String filename = System.currentTimeMillis() + "_" + finalFile.getOriginalFilename();
        String rootDir = System.getProperty("user.dir");

        File uploadDir = new File(rootDir + File.separator + "uploads");
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        File targetFile = new File(uploadDir + File.separator + filename);
        finalFile.transferTo(targetFile);

        String fileUrl = "https://pawpaw-app.up.railway.app/uploads/" + filename;
        currentUser.setCoverUrl(fileUrl);
        userRepository.save(currentUser);

        return ResponseEntity.ok(Map.of(
                "coverPhoto", fileUrl,
                "message", "Cover updated successfully"
        ));
    }


    private UserResponse mapToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setVerified(user.isVerified());
        response.setBio(user.getBio());
        response.setLocation(user.getLocation() != null ? user.getLocation() : "Not Specified");

        response.setUsername(user.getEmail());
        response.setProfilePicture(user.getAvatarUrl() != null ? user.getAvatarUrl() : "https://pawpaw-app.up.railway.app/uploads/default-avatar.jpg");
        response.setCoverPhoto(user.getCoverUrl() != null ? user.getCoverUrl() : "https://pawpaw-app.up.railway.app/uploads/default-cover.jpg");

        response.setBorn("2004-05-12");
        response.setStatus("Single");
        response.setEducation("Computer Science");
        response.setGraduationYear("2026");
        response.setFollowersCount(1500);
        response.setFollowingCount(420);
        response.setPostsCount(12);

        return response;
    }
}