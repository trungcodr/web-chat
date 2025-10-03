package com.example.project_chat.service;

import com.example.project_chat.dto.friend.UpdateProfileRequestDTO;
import com.example.project_chat.dto.response.UserResponseDTO;

public interface UserService {
    UserResponseDTO getCurrentUser();
    UserResponseDTO updateUser(UpdateProfileRequestDTO requestDTO);
}
