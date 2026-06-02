package com.PAWPAW.pawpaw.community.controller;

import com.PAWPAW.pawpaw.auth.entity.User;
import com.PAWPAW.pawpaw.community.dto.PostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PostController {


    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PostResponse>> getUserPosts(@PathVariable Long userId) {
        PostResponse mockPost = PostResponse.builder()
                .id("post_111")
                .author(PostResponse.AuthorDto.builder()
                        .username("menna_all_usama")
                        .profilePicture("https://pawpaw-app.up.railway.app/uploads/default-avatar.jpg")
                        .build())
                .createdAt(LocalDateTime.now())
                .content("My cute pet! 🐾")
                .mediaUrl("https://pawpaw-app.up.railway.app/uploads/default-cover.jpg")
                .likesCount(25)
                .liked(false)
                .comments(List.of(
                        new PostResponse.CommentDto("So nice!"),
                        new PostResponse.CommentDto("Amazing paw!")
                ))
                .build();
        return ResponseEntity.ok(List.of(mockPost));
    }


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponse> createNewFeedPost(
            @RequestParam("content") String content,
            @RequestParam(value = "image", required = false) MultipartFile file) throws IOException {

        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String fileUrl = null;

        if (file != null && !file.isEmpty()) {
            String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            String rootDir = System.getProperty("user.dir");
            File targetFile = new File(rootDir + File.separator + "uploads" + File.separator + filename);
            file.transferTo(targetFile);
            fileUrl = "https://pawpaw-app.up.railway.app/uploads/" + filename;
        }

        PostResponse newPost = PostResponse.builder()
                .id("post_" + System.currentTimeMillis())
                .author(PostResponse.AuthorDto.builder()
                        .username(currentUser.getEmail())
                        .profilePicture(currentUser.getAvatarUrl() != null ? currentUser.getAvatarUrl() : "https://pawpaw-app.up.railway.app/uploads/default-avatar.jpg")
                        .build())
                .createdAt(LocalDateTime.now())
                .content(content)
                .mediaUrl(fileUrl)
                .likesCount(0)
                .liked(false)
                .comments(List.of())
                .build();

        return ResponseEntity.ok(newPost);
    }
}