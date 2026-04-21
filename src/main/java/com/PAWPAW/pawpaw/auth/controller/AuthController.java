package com.PAWPAW.pawpaw.auth.controller;
import com.PAWPAW.pawpaw.auth.dto.UserResponse;
import com.PAWPAW.pawpaw.auth.entity.User;
import org.springframework.security.core.Authentication;
import com.PAWPAW.pawpaw.auth.dto.AuthResponse;
import com.PAWPAW.pawpaw.auth.dto.LoginRequest;
import com.PAWPAW.pawpaw.auth.dto.RegisterRequest;
import com.PAWPAW.pawpaw.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

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
        User user = (User) authentication.getPrincipal();
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setVerified(user.isVerified());
        return ResponseEntity.ok(response);
    }
}