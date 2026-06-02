package com.PAWPAW.pawpaw.community.controller;

import com.PAWPAW.pawpaw.auth.entity.User;
import com.PAWPAW.pawpaw.common.CloudinaryService;
import com.PAWPAW.pawpaw.community.dto.CommentRequest;
import com.PAWPAW.pawpaw.community.dto.CommentResponse;
import com.PAWPAW.pawpaw.community.dto.PostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final CloudinaryService cloudinaryService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PostResponse>> getUserPosts(@PathVariable Long userId) {

        CommentResponse mockComment1 = new CommentResponse();
        mockComment1.setId(1L);
        mockComment1.setContent("So nice! 🐾");
        mockComment1.setUserName("menna_all_usama");
        mockComment1.setCreatedAt(LocalDateTime.now());

        CommentResponse mockComment2 = new CommentResponse();
        mockComment2.setId(2L);
        mockComment2.setContent("Amazing paw! ❤️");
        mockComment2.setUserName("john_doe");
        mockComment2.setCreatedAt(LocalDateTime.now());

        PostResponse mockPost = PostResponse.builder()
                .id("post_111")
                .author(PostResponse.AuthorDto.builder()
                        .username("menna_all_usama")
                        .profilePicture("https://res.cloudinary.com/dqscucvxs/image/upload/pawpaw/default-avatar.jpg")
                        .build())
                .createdAt(LocalDateTime.now())
                .content("My cute pet! 🐾")
                .mediaUrl("https://res.cloudinary.com/dqscucvxs/image/upload/pawpaw/default-cover.jpg")
                .likesCount(25)
                .liked(false)
                .comments(List.of(mockComment1, mockComment2))
                .build();

        return ResponseEntity.ok(List.of(mockPost));
    }

    @PostMapping
    public ResponseEntity<PostResponse> createNewFeedPost(
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "text", required = false) String text,
            @RequestBody(required = false) Map<String, Object> body,
            @RequestParam(value = "file", required = false) MultipartFile file1,
            @RequestParam(value = "image", required = false) MultipartFile file2) throws IOException {

        String finalContent = content != null ? content :
                text != null ? text :
                        body != null ? (String) body.get("content") : null;

        MultipartFile finalFile = (file1 != null) ? file1 : file2;
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String fileUrl = null;

        if (finalFile != null && !finalFile.isEmpty()) {
            fileUrl = cloudinaryService.uploadFile(finalFile);
        }

        PostResponse newPost = PostResponse.builder()
                .id("post_" + System.currentTimeMillis())
                .author(PostResponse.AuthorDto.builder()
                        .username(currentUser.getEmail())
                        .profilePicture(currentUser.getAvatarUrl() != null ?
                                currentUser.getAvatarUrl() :
                                "https://res.cloudinary.com/dqscucvxs/image/upload/pawpaw/default-avatar.jpg")
                        .build())
                .createdAt(LocalDateTime.now())
                .content(finalContent)
                .mediaUrl(fileUrl)
                .likesCount(0)
                .liked(false)
                .comments(List.of())
                .build();

        return ResponseEntity.ok(newPost);
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<?> likePost(@PathVariable String postId) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(Map.of(
                "message", "Post like status updated successfully",
                "postId", postId,
                "userId", currentUser.getId()
        ));
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable String postId,
            @RequestBody CommentRequest request) {

        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        CommentResponse response = new CommentResponse();
        response.setId(101L);
        response.setContent(request.getContent());
        response.setUserId(currentUser.getId());
        response.setUserName(currentUser.getFullName());
        response.setCreatedAt(LocalDateTime.now());
        response.setReplies(List.of());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable String postId) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(Map.of(
                "message", "Post deleted successfully",
                "postId", postId
        ));
    }
}