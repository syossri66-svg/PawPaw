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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



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
                .map(user -> mapToResponse(user))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private UserResponse mapToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setVerified(user.isVerified());
        return response;
    }
}