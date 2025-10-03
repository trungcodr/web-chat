package com.example.project_chat.mapper;

import com.example.project_chat.dto.response.FriendRequestResponseDTO;
import com.example.project_chat.dto.response.SentFriendRequestResponseDTO;
import com.example.project_chat.entity.Friend;
import com.example.project_chat.entity.User;
import com.example.project_chat.repository.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class FriendMapper {
    private final UserRepository userRepository;
    public FriendMapper(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public FriendRequestResponseDTO toFriendRequestResponseDTO(Friend friendRequest) {
        User sender = userRepository.findById(friendRequest.getUserId())
                .orElse(null);

        FriendRequestResponseDTO dto = new FriendRequestResponseDTO();
        dto.setFriendRequestId(friendRequest.getId());

        if (sender != null) {
            dto.setSenderId(sender.getId());
            dto.setSenderDisplayName(sender.getDisplayName());
            dto.setSenderAvatarUrl(sender.getAvatarUrl());
        }
        return dto;
    }

    public SentFriendRequestResponseDTO toSentFriendRequestResponseDTO(Friend friendRequest) {
        User receiver = userRepository.findById(friendRequest.getFriendId()).orElse(null);
        SentFriendRequestResponseDTO dto = new SentFriendRequestResponseDTO();
        dto.setFriendRequestId(friendRequest.getId());
        if (receiver != null) {
            dto.setReceiverId(receiver.getId());
            dto.setReceiverDisplayName(receiver.getDisplayName());
            dto.setReceiverAvatarUrl(receiver.getAvatarUrl());
        }
        return dto;
    }
}
