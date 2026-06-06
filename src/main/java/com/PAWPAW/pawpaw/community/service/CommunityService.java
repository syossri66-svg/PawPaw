package com.PAWPAW.pawpaw.community.service;

import com.PAWPAW.pawpaw.admin.dto.UserSummary;
import com.PAWPAW.pawpaw.auth.entity.User;
import com.PAWPAW.pawpaw.auth.repository.UserRepository;
import com.PAWPAW.pawpaw.common.CloudinaryService;
import com.PAWPAW.pawpaw.community.dto.*;
import com.PAWPAW.pawpaw.community.entity.*;
import com.PAWPAW.pawpaw.community.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
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
    private final StoryRepository storyRepository;


    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ✅ Helper method - لو رقم يجيب بالـ ID، لو نص يجيب بالـ fullName
    private User resolveUser(String identifier) {
        try {
            Long id = Long.parseLong(identifier);
            return userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));
        } catch (NumberFormatException e) {
            return userRepository.findByFullName(identifier)
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }
    }

    public PostResponse createPost(PostRequest request) {
        User user = getCurrentUser();
        String uploadedImageUrl = null;

        try {
            if (request.getImage() != null && !request.getImage().isEmpty()) {
                uploadedImageUrl = cloudinaryService.uploadFile(request.getImage());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image", e);
        }

        Post post = Post.builder()
                .content(request.getContent())
                .imageUrl(uploadedImageUrl)
                .user(user)
                .build();

        return mapToPostResponse(postRepository.save(post), user);
    }

    public List<PostResponse> getAllPosts() {
        User currentUser = getCurrentUser();
        return postRepository.findAllPostsWithUser()
                .stream()
                .map(post -> mapToPostResponse(post, currentUser))
                .collect(Collectors.toList());
    }

    public void deletePost(Long postId) {
        postRepository.deleteById(postId);
    }

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

    private PostResponse mapToPostResponse(Post post, User currentUser) {
        PostResponse response = new PostResponse();
        response.setId(post.getId());
        response.setCreatedAt(post.getCreatedAt());

        PostResponse.UserInfo userInfo = new PostResponse.UserInfo();
        userInfo.setId(post.getUser().getId());
        userInfo.setName(post.getUser().getFullName());
        userInfo.setAvatar(post.getUser().getAvatarUrl());
        userInfo.setFollowing(followRepository.findByFollowerIdAndFollowingId(
                currentUser.getId(), post.getUser().getId()).isPresent());
        response.setUser(userInfo);

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
        User currentUser = getCurrentUser();
        return postRepository.findByUserIdWithUser(userId)
                .stream()
                .map(post -> mapToPostResponse(post, currentUser))
                .collect(Collectors.toList());
    }

    // ✅ toggleFollow بيقبل String (رقم أو اسم)
    public String toggleFollow(String identifier) {
        User currentUser = getCurrentUser();
        User targetUser = resolveUser(identifier);

        var existing = followRepository.findByFollowerIdAndFollowingId(
                currentUser.getId(), targetUser.getId());

        if (existing.isPresent()) {
            followRepository.delete(existing.get());
            return "Unfollowed";
        } else {
            followRepository.save(Follow.builder()
                    .follower(currentUser)
                    .following(targetUser)
                    .build());
            return "Followed";
        }
    }

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

    public List<PostResponse> getSavedPosts() {
        User currentUser = getCurrentUser();
        return savedPostRepository.findByUserId(currentUser.getId())
                .stream()
                .map(sp -> mapToPostResponse(sp.getPost(), currentUser))
                .collect(Collectors.toList());
    }

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

    // ✅ getProfile بيقبل String (رقم أو اسم)
    public ProfileResponse getProfile(String identifier) {
        User currentUser = getCurrentUser();
        User targetUser = resolveUser(identifier);

        long followersCount = followRepository.countByFollowingId(targetUser.getId());
        long followingCount = followRepository.countByFollowerId(targetUser.getId());

        boolean isFollowing = followRepository.findByFollowerIdAndFollowingId(
                currentUser.getId(), targetUser.getId()).isPresent();

        return ProfileResponse.builder()
                .id(targetUser.getId())
                .fullName(targetUser.getFullName())
                .email(targetUser.getEmail())
                .avatarUrl(targetUser.getAvatarUrl())
                .coverUrl(targetUser.getCoverUrl())
                .bio(targetUser.getBio())
                .location(targetUser.getLocation())
                .followersCount(followersCount)
                .followingCount(followingCount)
                .isFollowing(isFollowing)
                .build();
    }

    // ✅ getUserPosts بيقبل String (رقم أو اسم)
    public List<PostResponse> getUserPosts(String identifier) {
        User targetUser = resolveUser(identifier);
        return getPostsByUser(targetUser.getId());
    }

    public List<ProfileResponse> getUserFriends(Long userId) {
        User currentUser = getCurrentUser();

        return followRepository.findByFollowerId(userId)
                .stream()
                .map(follow -> {
                    User friend = follow.getFollowing();

                    long followers = followRepository.countByFollowingId(friend.getId());
                    long following = followRepository.countByFollowerId(friend.getId());
                    boolean isFollowingFriend = followRepository.findByFollowerIdAndFollowingId(
                            currentUser.getId(), friend.getId()).isPresent();

                    return ProfileResponse.builder()
                            .id(friend.getId())
                            .fullName(friend.getFullName())
                            .email(friend.getEmail())
                            .avatarUrl(friend.getAvatarUrl())
                            .coverUrl(friend.getCoverUrl())
                            .bio(friend.getBio())
                            .location(friend.getLocation())
                            .followersCount(followers)
                            .followingCount(following)
                            .isFollowing(isFollowingFriend)
                            .build();
                })
                .collect(Collectors.toList());
    }

    public PostResponse createNewPostWithImage(String content, MultipartFile file) {
        User user = getCurrentUser();
        String uploadedImageUrl = null;

        try {
            if (file != null && !file.isEmpty()) {
                uploadedImageUrl = cloudinaryService.uploadFile(file);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload post image to Cloudinary", e);
        }

        Post post = Post.builder()
                .content(content)
                .imageUrl(uploadedImageUrl)
                .user(user)
                .build();

        Post savedPost = postRepository.save(post);
        return mapToPostResponse(savedPost, user);
    }
    // منطق الـ Stories
    public List<StoryResponse> getAllActiveStories() {
        return storyRepository.findByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime.now().minusHours(24))
                .stream().map(this::mapToStoryResponse).collect(Collectors.toList());
    }

    // ... داخل CommunityService.java ...

    public StoryResponse uploadNewStory(MultipartFile file, String caption) {
        User user = getCurrentUser();
        String url;

        try {
            // 🔥 تم إضافة الـ try-catch للتعامل مع الـ IOException
            url = cloudinaryService.uploadFile(file);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload story image: " + e.getMessage());
        }

        Story story = Story.builder()
                .user(user)
                .mediaUrl(url)
                .caption(caption)
                .build();

        Story savedStory = storyRepository.save(story);
        return mapToStoryResponse(savedStory);
    }

    private StoryResponse mapToStoryResponse(Story story) {
        return StoryResponse.builder()
                .id(story.getId())
                // 🔥 التصحيح: استخدم الـ UserSummary الأصلي مش اللي جوه الـ AiChatResponse
                .user(UserSummary.builder()
                        .id(story.getUser().getId())
                        .fullName(story.getUser().getFullName())
                        .avatarUrl(story.getUser().getAvatarUrl())
                        .build())
                .mediaUrl(story.getMediaUrl())
                .caption(story.getCaption())
                .createdAt(story.getCreatedAt())
                .build();
    }


}