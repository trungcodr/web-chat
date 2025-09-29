package com.example.project_chat.service.impl;

import com.example.project_chat.common.exception.ResourceNotFoundException;
import com.example.project_chat.dto.UpdateProfileRequestDTO;
import com.example.project_chat.dto.response.UserResponseDTO;
import com.example.project_chat.entity.User;
import com.example.project_chat.mapper.UserMapper;
import com.example.project_chat.repository.UserRepository;
import com.example.project_chat.service.FileStorageService;
import com.example.project_chat.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    public UserServiceImpl(UserRepository userRepository, FileStorageService fileStorageService) {
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    @Override
    public UserResponseDTO getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay nguoi dung trong email: " + email));
        return UserMapper.toUserResponseDTO(user);
    }

    @Override
    public UserResponseDTO updateUser(UpdateProfileRequestDTO requestDTO) {
        //Lay thong tin nugoi dung hien tai
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay nguoi voi email: " + email));

        if (requestDTO.getDisplayName() != null) {
            currentUser.setDisplayName(requestDTO.getDisplayName());
        }
        if (requestDTO.getGender() != null) {
            currentUser.setGender(requestDTO.getGender());
        }
        if (requestDTO.getDateOfBirth() != null) {
            currentUser.setDateOfBirth(requestDTO.getDateOfBirth());
        }
        if (requestDTO.getAvatarFile() != null && !requestDTO.getAvatarFile().isEmpty()) {
            String fileName = fileStorageService.uploadFile(requestDTO.getAvatarFile());
            String avatarUrl = fileName;
            currentUser.setAvatarUrl(avatarUrl);
        }
        User updatedUser = userRepository.save(currentUser);
        return UserMapper.toUserResponseDTO(updatedUser);
    }


}
