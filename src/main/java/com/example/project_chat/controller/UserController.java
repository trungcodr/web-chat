package com.example.project_chat.controller;

import com.example.project_chat.dto.friend.UpdateProfileRequestDTO;
import com.example.project_chat.dto.response.ApiResponse;
import com.example.project_chat.dto.response.UserResponseDTO;
import com.example.project_chat.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getCurrentUser() {
        UserResponseDTO user = userService.getCurrentUser();
        ApiResponse<UserResponseDTO> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Lay thong tin nguoi dung thanh cong!",
                user
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/me/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserResponseDTO>> updateUser(@ModelAttribute UpdateProfileRequestDTO requestDTO) {
        UserResponseDTO updateUser = userService.updateUser(requestDTO);
        ApiResponse<UserResponseDTO> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Cap nhat thong tin thanh cong!",
                updateUser
        );
        return ResponseEntity.ok(response);
    }
}
