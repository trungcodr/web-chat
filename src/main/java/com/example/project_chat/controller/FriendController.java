package com.example.project_chat.controller;

import com.example.project_chat.dto.friend.FriendResponseDTO;
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
                "Gửi lời mời kết bạn thành công!",
                null
        );
        return  ResponseEntity.ok(response);
    }

    @GetMapping("/requests/received")
    public ResponseEntity<ApiResponse<List<FriendRequestResponseDTO>>> getReceivedFriendRequests() {
        List<FriendRequestResponseDTO> requests = friendService.getReceivedFriendRequests();
        ApiResponse<List<FriendRequestResponseDTO>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Lấy danh sách lời mời kết bạn đã nhận thành công!",
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
                ? "Chấp nhận lời mời kết bạn!"
                : "Từ chối lời mời kết bạn!";

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
    public ResponseEntity<ApiResponse<List<FriendResponseDTO>>> getFriendList() {
        List<FriendResponseDTO> friendList = friendService.getFriendList();
        ApiResponse<List<FriendResponseDTO>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Lấy danh sách lời mời đã gửi thành công!",
                friendList
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/requests/{requestId}")
    public ResponseEntity<ApiResponse<?>> cancelFriendRequest(@PathVariable Integer requestId) {
        friendService.cancelFriendRequest(requestId);
        ApiResponse<?> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Thu hồi lời mời kết bạn thành công!",
                null
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{friendId}")
    public ResponseEntity<ApiResponse<?>> unFriend(@PathVariable Integer friendId) {
        friendService.unFriend(friendId);
        ApiResponse<?> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Hủy kết bạn thành công!",
                null
        );
        return ResponseEntity.ok(response);
    }

}
