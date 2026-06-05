package com.PAWPAW.pawpaw.community.controller;

import com.PAWPAW.pawpaw.community.dto.*;
import com.PAWPAW.pawpaw.community.service.CommunityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    // --- Post Endpoints ---

    @PostMapping(value = "/posts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponse> createPost(
            @RequestParam("content") String content,
            @RequestParam(value = "file", required = false) MultipartFile file1,
            @RequestParam(value = "image", required = false) MultipartFile file2) {

        MultipartFile finalFile = (file1 != null) ? file1 : file2;
        return ResponseEntity.ok(communityService.createNewPostWithImage(content, finalFile));
    }

    @GetMapping("/posts")
    public ResponseEntity<List<PostResponse>> getAllPosts() {
        return ResponseEntity.ok(communityService.getAllPosts());
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        communityService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<String> toggleLike(@PathVariable Long postId) {
        return ResponseEntity.ok(communityService.toggleLike(postId));
    }

    @PostMapping("/posts/{postId}/save")
    public ResponseEntity<String> toggleSave(@PathVariable Long postId) {
        return ResponseEntity.ok(communityService.toggleSave(postId));
    }

    @GetMapping("/posts/saved")
    public ResponseEntity<List<PostResponse>> getSavedPosts() {
        return ResponseEntity.ok(communityService.getSavedPosts());
    }

    // --- Profile & User Endpoints ---

    // ✅ String بدل Long - بيقبل رقم أو اسم
    @GetMapping("/profiles/{userId}")
    public ResponseEntity<ProfileResponse> getProfile(@PathVariable String userId) {
        return ResponseEntity.ok(communityService.getProfile(userId));
    }

    // ✅ String بدل Long - بيقبل رقم أو اسم
    @GetMapping("/profiles/{userId}/posts")
    public ResponseEntity<List<PostResponse>> getUserPosts(@PathVariable String userId) {
        return ResponseEntity.ok(communityService.getUserPosts(userId));
    }

    @GetMapping("/profiles/{userId}/friends")
    public ResponseEntity<List<ProfileResponse>> getUserFriends(@PathVariable Long userId) {
        return ResponseEntity.ok(communityService.getUserFriends(userId));
    }

    // ✅ String بدل Long - بيقبل رقم أو اسم
    @PostMapping("/profiles/{userId}/follow")
    public ResponseEntity<String> toggleFollow(@PathVariable String userId) {
        return ResponseEntity.ok(communityService.toggleFollow(userId));
    }

    // --- Comment Endpoints ---

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentResponse> addComment(@PathVariable Long postId,
                                                      @Valid @RequestBody CommentRequest request) {
        return ResponseEntity.ok(communityService.addComment(postId, request));
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long postId) {
        return ResponseEntity.ok(communityService.getPostComments(postId));
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        communityService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/comments/{commentId}/reply")
    public ResponseEntity<CommentResponse> replyToComment(@PathVariable Long commentId,
                                                          @Valid @RequestBody CommentRequest request) {
        return ResponseEntity.ok(communityService.replyToComment(commentId, request));
    }
}