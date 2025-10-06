package com.example.project_chat.service;

import com.example.project_chat.dto.friend.FriendResponseDTO;
import com.example.project_chat.dto.friend.FriendRequestDTO;
import com.example.project_chat.dto.friend.UpdateFriendRequestDTO;
import com.example.project_chat.dto.response.FriendRequestResponseDTO;
import com.example.project_chat.dto.response.SentFriendRequestResponseDTO;


import java.util.List;

public interface FriendService {
    void sendFriendRequest(FriendRequestDTO friendRequestDTO);
    List<FriendRequestResponseDTO> getReceivedFriendRequests();
    void respondToFriendRequest(Integer requestId, UpdateFriendRequestDTO requestDTO);
    List<SentFriendRequestResponseDTO> getSentFriendRequests();
    List<FriendResponseDTO> getFriendList();
}
