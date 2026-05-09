package com.PAWPAW.pawpaw.community.controller;

import com.PAWPAW.pawpaw.community.dto.*;
import com.PAWPAW.pawpaw.community.service.CommunityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    @PostMapping("/posts")
    public ResponseEntity<PostResponse> createPost(@Valid @RequestBody PostRequest request) {
        return ResponseEntity.ok(communityService.createPost(request));
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

    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<String> toggleLike(@PathVariable Long postId) {
        return ResponseEntity.ok(communityService.toggleLike(postId));
    }
    @PostMapping("/api/users/follow/{id}")
    public ResponseEntity<String> toggleFollow(@PathVariable Long id) {
        return ResponseEntity.ok(communityService.toggleFollow(id));
    }
    @PostMapping("/posts/{postId}/save")
    public ResponseEntity<String> toggleSave(@PathVariable Long postId) {
        return ResponseEntity.ok(communityService.toggleSave(postId));
    }

    @GetMapping("/posts/saved")
    public ResponseEntity<List<PostResponse>> getSavedPosts() {
        return ResponseEntity.ok(communityService.getSavedPosts());
    }

    @PostMapping("/comments/{commentId}/reply")
    public ResponseEntity<CommentResponse> replyToComment(@PathVariable Long commentId,
                                                          @Valid @RequestBody CommentRequest request) {
        return ResponseEntity.ok(communityService.replyToComment(commentId, request));
    }
}