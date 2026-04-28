package com.PAWPAW.pawpaw.group.controller;

import com.PAWPAW.pawpaw.group.dto.GroupRequest;
import com.PAWPAW.pawpaw.group.dto.GroupResponse;
import com.PAWPAW.pawpaw.group.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(@Valid @RequestBody GroupRequest request) {
        return ResponseEntity.ok(groupService.createGroup(request));
    }

    @GetMapping
    public ResponseEntity<List<GroupResponse>> getAllGroups() {
        return ResponseEntity.ok(groupService.getAllGroups());
    }

    @GetMapping("/my")
    public ResponseEntity<List<GroupResponse>> getMyGroups() {
        return ResponseEntity.ok(groupService.getMyGroups());
    }

    @PostMapping("/{groupId}/join")
    public ResponseEntity<GroupResponse> joinGroup(@PathVariable Long groupId) {
        return ResponseEntity.ok(groupService.joinGroup(groupId));
    }

    @DeleteMapping("/{groupId}/leave")
    public ResponseEntity<Void> leaveGroup(@PathVariable Long groupId) {
        groupService.leaveGroup(groupId);
        return ResponseEntity.noContent().build();
    }
}