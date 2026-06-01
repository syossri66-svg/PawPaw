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
    private final FollowRepository followRepository;
    private final CloudinaryService cloudinaryService;
    private final SavedPostRepository savedPostRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public PostResponse createPost(PostRequest request) {
        User user = getCurrentUser();
        String uploadedImageUrl = null;

        if (request.getImage() != null && !request.getImage().isEmpty()) {
            uploadedImageUrl = cloudinaryService.uploadFile(request.getImage());
        }

        Post post = Post.builder()
                .content(request.getContent())
                .imageUrl(uploadedImageUrl)
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

    private PostResponse mapToPostResponse(Post post) {
        User currentUser = getCurrentUser();

        PostResponse response = new PostResponse();
        response.setId(post.getId());
        response.setCreatedAt(post.getCreatedAt());

        // User Info
        PostResponse.UserInfo userInfo = new PostResponse.UserInfo();
        userInfo.setId(post.getUser().getId());
        userInfo.setName(post.getUser().getFullName());
        userInfo.setAvatar(post.getUser().getAvatarUrl());
        userInfo.setFollowing(followRepository.findByFollowerIdAndFollowingId(
                currentUser.getId(), post.getUser().getId()).isPresent());
        response.setUser(userInfo);

        // ✅ رجعنا الـ ContentInfo الـ Object شغال هنا تمام ومتوافق مع الـ DTO
        PostResponse.ContentInfo contentInfo = new PostResponse.ContentInfo();
        contentInfo.setText(post.getContent());
        contentInfo.setMediaUrl(post.getImageUrl());
        contentInfo.setType(post.getImageUrl() != null ? "image" : null);
        response.setContent(contentInfo);

        PostResponse.StatsInfo statsInfo = new PostResponse.StatsInfo();
        statsInfo.setLikes(likeRepository.countByPostId(post.getId()));
        statsInfo.setComments(commentRepository.findByPostId(post.getId()).size());
        statsInfo.setShares(0);
        response.setStats(statsInfo);

        response.setLiked(likeRepository.findByUserIdAndPostId(
                currentUser.getId(), post.getId()).isPresent());

        response.setSaved(savedPostRepository.findByUserIdAndPostId(
                currentUser.getId(), post.getId()).isPresent());

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

    public List<PostResponse> getPostsByUser(Long userId) {
        return postRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::mapToPostResponse).collect(Collectors.toList());
    }

    public String toggleFollow(Long targetUserId) {
        User currentUser = getCurrentUser();
        var existing = followRepository.findByFollowerIdAndFollowingId(
                currentUser.getId(), targetUserId);

        if (existing.isPresent()) {
            followRepository.delete(existing.get());
            return "Unfollowed";
        } else {
            User target = userRepository.findById(targetUserId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            followRepository.save(Follow.builder()
                    .follower(currentUser)
                    .following(target)
                    .build());
            return "Followed";
        }
    }

    // Save Toggle
    public String toggleSave(Long postId) {
        User user = getCurrentUser();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        var existing = savedPostRepository.findByUserIdAndPostId(user.getId(), postId);
        if (existing.isPresent()) {
            savedPostRepository.delete(existing.get());
            return "Unsaved";
        } else {
            savedPostRepository.save(SavedPost.builder()
                    .user(user).post(post).build());
            return "Saved";
        }
    }

    // Get Saved Posts
    public List<PostResponse> getSavedPosts() {
        User user = getCurrentUser();
        return savedPostRepository.findByUserId(user.getId())
                .stream()
                .map(sp -> mapToPostResponse(sp.getPost()))
                .collect(Collectors.toList());
    }

    // Reply to Comment
    public CommentResponse replyToComment(Long parentId, CommentRequest request) {
        User user = getCurrentUser();
        Comment parent = commentRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        Comment reply = Comment.builder()
                .content(request.getContent())
                .user(user)
                .post(parent.getPost())
                .parent(parent)
                .build();
        return mapToCommentResponse(commentRepository.save(reply));
    }
}