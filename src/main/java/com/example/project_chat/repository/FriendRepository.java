package com.example.project_chat.repository;

import com.example.project_chat.common.constants.FriendStatus;
import com.example.project_chat.entity.Friend;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Integer> {
    Optional<Friend> findByUserIdAndFriendId(Integer userId, Integer friendId);

    List<Friend> findByFriendIdAndStatus(Integer friendId, FriendStatus status);
    List<Friend> findByUserIdAndStatus(Integer userId, FriendStatus status);
}
