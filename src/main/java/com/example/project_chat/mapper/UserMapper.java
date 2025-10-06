package com.example.project_chat.mapper;

import com.example.project_chat.dto.friend.FriendResponseDTO;
import com.example.project_chat.dto.response.UserResponseDTO;
import com.example.project_chat.entity.User;

public class UserMapper {
    public static UserResponseDTO toUserResponseDTO(User user) {
        if (user == null) {
            return null;
        }
        UserResponseDTO userResponseDTO = new UserResponseDTO();
        userResponseDTO.setId(user.getId());
        userResponseDTO.setEmail(user.getEmail());
        userResponseDTO.setDisplayName(user.getDisplayName());
        userResponseDTO.setAvatarUrl(user.getAvatarUrl());
        userResponseDTO.setDateOfBirth(user.getDateOfBirth());
        userResponseDTO.setGender(user.getGender());
        return userResponseDTO;
    }

    public static FriendResponseDTO toFriendInfoDTO(User user) {
        if (user == null) {
            return null;
        }
        FriendResponseDTO dto = new FriendResponseDTO();
        dto.setDisplayName(user.getDisplayName());
        dto.setAvatarUrl(user.getAvatarUrl());
        return dto;
    }
}
