package com.PAWPAW.pawpaw.friend.controller;

import com.PAWPAW.pawpaw.friend.dto.FriendResponse;
import com.PAWPAW.pawpaw.friend.entity.FriendStatus;
import com.PAWPAW.pawpaw.friend.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @PostMapping("/request/{receiverId}")
    public ResponseEntity<FriendResponse> sendRequest(@PathVariable Long receiverId) {
        return ResponseEntity.ok(friendService.sendFriendRequest(receiverId));
    }

    @PutMapping("/accept/{friendId}")
    public ResponseEntity<FriendResponse> respond(@PathVariable Long friendId,
                                                  @RequestParam FriendStatus status) {
        return ResponseEntity.ok(friendService.respondToRequest(friendId, status));
    }

    @GetMapping("/requests")
    public ResponseEntity<List<FriendResponse>> getPending() {
        return ResponseEntity.ok(friendService.getPendingRequests());
    }

    @GetMapping
    public ResponseEntity<List<FriendResponse>> getMyFriends() {
        return ResponseEntity.ok(friendService.getMyFriends());
    }
    @GetMapping("/suggestions")
    public ResponseEntity<?> getSuggestions() {
        return ResponseEntity.ok(friendService.getSuggestions());
    }

    @DeleteMapping("/{friendId}")
    public ResponseEntity<Void> removeFriend(@PathVariable Long friendId) {
        friendService.removeFriend(friendId);
        return ResponseEntity.noContent().build();
    }
    // ✅ accept
    @PostMapping("/accept/{friendId}")
    public ResponseEntity<FriendResponse> acceptRequest(@PathVariable Long friendId) {
        return ResponseEntity.ok(friendService.respondToRequest(friendId, FriendStatus.ACCEPTED));
    }

    // ✅ decline
    @PostMapping("/decline/{friendId}")
    public ResponseEntity<FriendResponse> declineRequest(@PathVariable Long friendId) {
        return ResponseEntity.ok(friendService.respondToRequest(friendId, FriendStatus.REJECTED));
    }


}