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
@CrossOrigin(origins = "*")
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

    // 2️⃣ تعديل البيانات الشخصية الشخصية (PATCH /api/auth/me)
    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateProfileDetails(@RequestBody Map<String, Object> updates) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (updates.containsKey("location")) currentUser.setLocation((String) updates.get("location"));
        if (updates.containsKey("bio")) currentUser.setBio((String) updates.get("bio"));

        userRepository.save(currentUser);
        return ResponseEntity.ok(mapToResponse(currentUser));
    }

    // 3️⃣ تعديل الـ Avatar وصور الملفات (PATCH /api/auth/me/avatar)
    @PatchMapping("/me/avatar")
    public ResponseEntity<Map<String, String>> updateAvatar(@RequestParam("avatar") MultipartFile file) throws IOException {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        String rootDir = System.getProperty("user.dir");
        File targetFile = new File(rootDir + File.separator + "uploads" + File.separator + filename);
        file.transferTo(targetFile);

        String fileUrl = "https://pawpaw-app.up.railway.app/uploads/" + filename;
        currentUser.setAvatarUrl(fileUrl);
        userRepository.save(currentUser);

        return ResponseEntity.ok(Map.of("profilePicture", fileUrl));
    }

    // 3️⃣ تعديل الـ Cover وصور الخلفية (PATCH /api/auth/me/cover)
    @PatchMapping("/me/cover")
    public ResponseEntity<Map<String, String>> updateCover(@RequestParam("cover") MultipartFile file) throws IOException {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        String rootDir = System.getProperty("user.dir");
        File targetFile = new File(rootDir + File.separator + "uploads" + File.separator + filename);
        file.transferTo(targetFile);

        String fileUrl = "https://pawpaw-app.up.railway.app/uploads/" + filename;
        currentUser.setCoverUrl(fileUrl);
        userRepository.save(currentUser);

        return ResponseEntity.ok(Map.of("coverPhoto", fileUrl));
    }

    // 🎯 تحديث ميثود الـ Mapping لتملأ كل الداتا اللي منة مستنياها تلقائياً
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