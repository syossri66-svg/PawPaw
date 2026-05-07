package com.PAWPAW.pawpaw.friend.service;

import com.PAWPAW.pawpaw.auth.entity.User;
import com.PAWPAW.pawpaw.auth.repository.UserRepository;
import com.PAWPAW.pawpaw.friend.dto.FriendResponse;
import com.PAWPAW.pawpaw.friend.entity.Friend;
import com.PAWPAW.pawpaw.friend.entity.FriendStatus;
import com.PAWPAW.pawpaw.friend.repository.FriendRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public FriendResponse sendFriendRequest(Long receiverId) {
        User requester = getCurrentUser();
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        friendRepository.findFriendship(requester.getId(), receiverId)
                .ifPresent(f -> { throw new RuntimeException("Already friends or request pending"); });

        Friend friend = Friend.builder()
                .requester(requester)
                .receiver(receiver)
                .build();

        return mapToResponse(friendRepository.save(friend));
    }

    public FriendResponse respondToRequest(Long friendId, FriendStatus status) {
        Friend friend = friendRepository.findById(friendId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        friend.setStatus(status);
        return mapToResponse(friendRepository.save(friend));
    }

    public List<FriendResponse> getPendingRequests() {
        User user = getCurrentUser();
        return friendRepository.findByReceiverIdAndStatus(user.getId(), FriendStatus.PENDING)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<FriendResponse> getMyFriends() {
        User user = getCurrentUser();
        return friendRepository.findAllFriends(user.getId())
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private FriendResponse mapToResponse(Friend friend) {
        FriendResponse response = new FriendResponse();
        response.setId(friend.getId());
        response.setRequesterId(friend.getRequester().getId());
        response.setRequesterName(friend.getRequester().getFullName());
        response.setReceiverId(friend.getReceiver().getId());
        response.setReceiverName(friend.getReceiver().getFullName());
        response.setStatus(friend.getStatus());
        return response;
    }
    public List<Map<String, Object>> getSuggestions() {
        User user = getCurrentUser();
        return friendRepository.findSuggestions(user.getId())
                .stream()
                .map(u -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", u.getId());
                    map.put("name", u.getFullName());
                    map.put("avatar", null);
                    map.put("mutual", 0);
                    return map;
                })
                .collect(Collectors.toList());
    }

    public void removeFriend(Long friendId) {
        friendRepository.deleteById(friendId);
    }
}