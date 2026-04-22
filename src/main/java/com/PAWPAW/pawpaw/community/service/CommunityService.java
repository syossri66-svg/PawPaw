package com.PAWPAW.pawpaw.community.service;

import com.PAWPAW.pawpaw.auth.entity.User;
import com.PAWPAW.pawpaw.auth.repository.UserRepository;
import com.PAWPAW.pawpaw.community.dto.*;
import com.PAWPAW.pawpaw.community.entity.*;
import com.PAWPAW.pawpaw.community.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommunityService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Posts
    public PostResponse createPost(PostRequest request) {
        User user = getCurrentUser();
        Post post = Post.builder()
                .content(request.getContent())
                .imageUrl(request.getImageUrl())
                .user(user)
                .build();
        return mapToPostResponse(postRepository.save(post));
    }

    public List<PostResponse> getAllPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToPostResponse)
                .collect(Collectors.toList());
    }

    public void deletePost(Long postId) {
        postRepository.deleteById(postId);
    }

    // Comments
    public CommentResponse addComment(Long postId, CommentRequest request) {
        User user = getCurrentUser();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        Comment comment = Comment.builder()
                .content(request.getContent())
                .user(user)
                .post(post)
                .build();
        return mapToCommentResponse(commentRepository.save(comment));
    }

    public List<CommentResponse> getPostComments(Long postId) {
        return commentRepository.findByPostId(postId)
                .stream()
                .map(this::mapToCommentResponse)
                .collect(Collectors.toList());
    }

    public void deleteComment(Long commentId) {
        commentRepository.deleteById(commentId);
    }

    // Likes
    public String toggleLike(Long postId) {
        User user = getCurrentUser();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        var existingLike = likeRepository.findByUserIdAndPostId(user.getId(), postId);

        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());
            return "Like removed";
        } else {
            Like like = Like.builder()
                    .user(user)
                    .post(post)
                    .build();
            likeRepository.save(like);
            return "Like added";
        }
    }

    // Mappers
    private PostResponse mapToPostResponse(Post post) {
        PostResponse response = new PostResponse();
        response.setId(post.getId());
        response.setContent(post.getContent());
        response.setImageUrl(post.getImageUrl());
        response.setUserId(post.getUser().getId());
        response.setUserName(post.getUser().getFullName());
        response.setLikesCount(likeRepository.countByPostId(post.getId()));
        response.setCommentsCount(commentRepository.findByPostId(post.getId()).size());
        response.setCreatedAt(post.getCreatedAt());
        return response;
    }

    private CommentResponse mapToCommentResponse(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        response.setUserId(comment.getUser().getId());
        response.setUserName(comment.getUser().getFullName());
        response.setPostId(comment.getPost().getId());
        response.setCreatedAt(comment.getCreatedAt());
        return response;
    }
}