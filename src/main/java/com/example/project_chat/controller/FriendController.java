package com.example.project_chat.controller;

import com.example.project_chat.dto.friend.FriendInfoDTO;
import com.example.project_chat.dto.friend.FriendRequestDTO;
import com.example.project_chat.dto.friend.UpdateFriendRequestDTO;
import com.example.project_chat.dto.response.ApiResponse;
import com.example.project_chat.dto.response.FriendRequestResponseDTO;
import com.example.project_chat.dto.response.SentFriendRequestResponseDTO;
import com.example.project_chat.service.FriendService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
public class FriendController {
    private final FriendService friendService;
    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }

    @PostMapping("/requests")
    public ResponseEntity<ApiResponse<?>> sendFriendRequest(@Valid @RequestBody FriendRequestDTO requestDTO) {
        friendService.sendFriendRequest(requestDTO);
        ApiResponse<?> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Gui loi moi ket ban thanh cong!",
                null
        );
        return  ResponseEntity.ok(response);
    }

    @GetMapping("/requests/received")
    public ResponseEntity<ApiResponse<List<FriendRequestResponseDTO>>> getReceivedFriendRequests() {
        List<FriendRequestResponseDTO> requests = friendService.getReceivedFriendRequests();
        ApiResponse<List<FriendRequestResponseDTO>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Lay danh sach loi moi ket ban da nhan thanh cong!",
                requests
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/requests/{requestId}")
    public ResponseEntity<ApiResponse<?>> respondToFriendRequest(
            @PathVariable Integer requestId,
            @Valid @RequestBody UpdateFriendRequestDTO request) {

        friendService.respondToFriendRequest(requestId, request);

        String message = request.getStatus() == com.example.project_chat.common.constants.FriendStatus.ACCEPTED
                ? "Chap nhan loi moi ket ban!"
                : "Tu choi loi moi ket ban!";

        ApiResponse<?> response = new ApiResponse<>(HttpStatus.OK.value(), message, null);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/requests/sent")
    public ResponseEntity<ApiResponse<List<SentFriendRequestResponseDTO>>> getSentFriendRequests() {
        List<SentFriendRequestResponseDTO> requests = friendService.getSentFriendRequests();
        ApiResponse<List<SentFriendRequestResponseDTO>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Lay danh sach loi moi da gui thanh cong!",
                requests
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FriendInfoDTO>>> getFriendList() {
        List<FriendInfoDTO> friendList = friendService.getFriendList();
        ApiResponse<List<FriendInfoDTO>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Lay danh sach ban be thanh cong!",
                friendList
        );
        return ResponseEntity.ok(response);
    }
}
