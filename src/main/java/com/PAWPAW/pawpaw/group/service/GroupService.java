package com.PAWPAW.pawpaw.group.service;

import com.PAWPAW.pawpaw.auth.entity.User;
import com.PAWPAW.pawpaw.auth.repository.UserRepository;
import com.PAWPAW.pawpaw.group.dto.GroupRequest;
import com.PAWPAW.pawpaw.group.dto.GroupResponse;
import com.PAWPAW.pawpaw.group.entity.Group;
import com.PAWPAW.pawpaw.group.entity.GroupMember;
import com.PAWPAW.pawpaw.group.repository.GroupMemberRepository;
import com.PAWPAW.pawpaw.group.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public GroupResponse createGroup(GroupRequest request) {
        User creator = getCurrentUser();
        Group group = Group.builder()
                .name(request.getName())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .creator(creator)
                .build();
        Group saved = groupRepository.save(group);

        GroupMember member = GroupMember.builder()
                .group(saved)
                .user(creator)
                .build();
        groupMemberRepository.save(member);

        return mapToResponse(saved);
    }

    public List<GroupResponse> getAllGroups() {
        return groupRepository.findAll()
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<GroupResponse> getMyGroups() {
        User user = getCurrentUser();
        return groupMemberRepository.findByUserId(user.getId())
                .stream().map(gm -> mapToResponse(gm.getGroup())).collect(Collectors.toList());
    }

    public GroupResponse joinGroup(Long groupId) {
        User user = getCurrentUser();
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        groupMemberRepository.findByGroupIdAndUserId(groupId, user.getId())
                .ifPresent(m -> { throw new RuntimeException("Already a member"); });

        GroupMember member = GroupMember.builder()
                .group(group)
                .user(user)
                .build();
        groupMemberRepository.save(member);
        return mapToResponse(group);
    }

    public void leaveGroup(Long groupId) {
        User user = getCurrentUser();
        groupMemberRepository.findByGroupIdAndUserId(groupId, user.getId())
                .ifPresent(groupMemberRepository::delete);
    }

    private GroupResponse mapToResponse(Group group) {
        GroupResponse response = new GroupResponse();
        response.setId(group.getId());
        response.setName(group.getName());
        response.setDescription(group.getDescription());
        response.setImageUrl(group.getImageUrl());
        response.setCreatorId(group.getCreator().getId());
        response.setCreatorName(group.getCreator().getFullName());
        response.setMembersCount(groupMemberRepository.findByGroupId(group.getId()).size());
        response.setCreatedAt(group.getCreatedAt());
        return response;
    }

    public GroupResponse getGroupById(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        GroupResponse response = mapToResponse(group);

        if (group.getPosts() != null) {
            List<com.PAWPAW.pawpaw.community.dto.PostResponse> postResponses = group.getPosts().stream()
                    .map(post -> {

                        com.PAWPAW.pawpaw.community.dto.PostResponse pr = new com.PAWPAW.pawpaw.community.dto.PostResponse();
                        pr.setId(post.getId());
                        pr.setCreatedAt(post.getCreatedAt());


                        com.PAWPAW.pawpaw.community.dto.PostResponse.UserInfo userInfo = new com.PAWPAW.pawpaw.community.dto.PostResponse.UserInfo();
                        if (post.getUser() != null) {
                            userInfo.setId(post.getUser().getId());
                            userInfo.setName(post.getUser().getFullName());
                            userInfo.setAvatar(post.getUser().getAvatarUrl());
                        }
                        pr.setUser(userInfo);


                        com.PAWPAW.pawpaw.community.dto.PostResponse.ContentInfo contentInfo = new com.PAWPAW.pawpaw.community.dto.PostResponse.ContentInfo();
                        contentInfo.setText(post.getContent());
                        contentInfo.setMediaUrl(post.getImageUrl());
                        contentInfo.setType(post.getImageUrl() != null ? "image" : null);
                        pr.setContent(contentInfo);


                        com.PAWPAW.pawpaw.community.dto.PostResponse.StatsInfo statsInfo = new com.PAWPAW.pawpaw.community.dto.PostResponse.StatsInfo();
                        statsInfo.setLikes(0);
                        statsInfo.setComments(0);
                        statsInfo.setShares(0);
                        pr.setStats(statsInfo);

                        return pr;
                    }).collect(Collectors.toList());

            response.setPosts(postResponses);
        }

        return response;
    }
}