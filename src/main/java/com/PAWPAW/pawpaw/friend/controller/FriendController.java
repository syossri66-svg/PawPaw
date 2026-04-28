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

    @PutMapping("/respond/{friendId}")
    public ResponseEntity<FriendResponse> respond(@PathVariable Long friendId,
                                                  @RequestParam FriendStatus status) {
        return ResponseEntity.ok(friendService.respondToRequest(friendId, status));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<FriendResponse>> getPending() {
        return ResponseEntity.ok(friendService.getPendingRequests());
    }

    @GetMapping
    public ResponseEntity<List<FriendResponse>> getMyFriends() {
        return ResponseEntity.ok(friendService.getMyFriends());
    }
}