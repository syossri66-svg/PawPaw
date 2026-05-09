package com.PAWPAW.pawpaw.friend.dto;

import com.PAWPAW.pawpaw.friend.entity.FriendStatus;
import lombok.Data;

@Data
public class FriendResponse {
    private Long id;
    private String name;
    private String avatar;
    private int mutual;
    private Long requesterId;
    private String requesterName;
    private Long receiverId;
    private String receiverName;
    private FriendStatus status;
}