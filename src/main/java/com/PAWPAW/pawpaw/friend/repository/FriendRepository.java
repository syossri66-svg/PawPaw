package com.PAWPAW.pawpaw.friend.repository;

import com.PAWPAW.pawpaw.friend.entity.Friend;
import com.PAWPAW.pawpaw.friend.entity.FriendStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {
    List<Friend> findByReceiverIdAndStatus(Long receiverId, FriendStatus status);
    List<Friend> findByRequesterIdAndStatus(Long requesterId, FriendStatus status);

    @Query("SELECT f FROM Friend f WHERE ((f.requester.id = :userId1 AND f.receiver.id = :userId2) OR (f.requester.id = :userId2 AND f.receiver.id = :userId1))")
    Optional<Friend> findFriendship(Long userId1, Long userId2);

    @Query("SELECT f FROM Friend f WHERE (f.requester.id = :userId OR f.receiver.id = :userId) AND f.status = 'ACCEPTED'")
    List<Friend> findAllFriends(Long userId);
}