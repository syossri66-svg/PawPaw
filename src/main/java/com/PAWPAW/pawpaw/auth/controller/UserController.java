package com.PAWPAW.pawpaw.auth.controller;
import com.PAWPAW.pawpaw.auth.dto.UpdateProfileRequest;
import com.PAWPAW.pawpaw.auth.dto.UserResponse;
import com.PAWPAW.pawpaw.auth.entity.User;
import com.PAWPAW.pawpaw.auth.service.AuthService;
import org.springframework.security.core.Authentication;
import com.PAWPAW.pawpaw.auth.dto.UserStatsResponse;
import com.PAWPAW.pawpaw.auth.service.UserService;
import com.PAWPAW.pawpaw.community.dto.PostResponse;
import com.PAWPAW.pawpaw.community.service.CommunityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final CommunityService communityService;
    private final AuthService authService;

    @GetMapping("/stats/{id}")
    public ResponseEntity<UserStatsResponse> getStats(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserStats(id));
    }

    @PostMapping("/follow/{id}")
    public ResponseEntity<Map<String, String>> toggleFollow(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of("status", userService.toggleFollow(id)));
    }

    @GetMapping("/is-following/{id}")
    public ResponseEntity<Map<String, Boolean>> isFollowing(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of("following", userService.isFollowing(id)));
    }

    @GetMapping("/posts/{id}")
    public ResponseEntity<List<PostResponse>> getUserPosts(@PathVariable Long id) {
        return ResponseEntity.ok(communityService.getPostsByUser(id));
    }


    @PutMapping("/update")
    public ResponseEntity<UserResponse> updateProfile(@RequestBody UpdateProfileRequest request,
                                                      Authentication authentication) {
        User current = (User) authentication.getPrincipal();
        return ResponseEntity.ok(authService.updateProfile(current.getId(), request));
    }
    @GetMapping("/profile/{id}")
    public ResponseEntity<UserResponse> getUserProfile(@PathVariable Long id) {
        return ResponseEntity.ok(authService.getUserProfile(id));
    }
}