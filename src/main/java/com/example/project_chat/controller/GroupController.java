package com.example.project_chat.controller;

import com.example.project_chat.dto.group.AddMemberRequestDTO;
import com.example.project_chat.dto.group.CreateGroupRequestDTO;
import com.example.project_chat.dto.group.UpdateGroupRequestDTO;
import com.example.project_chat.dto.message.ConversationSummaryDTO;
import com.example.project_chat.dto.response.ApiResponse;
import com.example.project_chat.service.ConversationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupController {
    private final ConversationService conversationService;
    public GroupController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ConversationSummaryDTO>> createGroupConversations(
            @Valid @ModelAttribute CreateGroupRequestDTO requestDTO) {
        ConversationSummaryDTO newGroup = conversationService.createGroupConversation(requestDTO);
        ApiResponse<ConversationSummaryDTO> response = new ApiResponse<>(
                HttpStatus.CREATED.value(),
                "Tạo nhóm chat thành công!",
                newGroup
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);

    }

    @PostMapping("/{groupId}/members")
    public ResponseEntity<ApiResponse<?>> addMembersToGroup(
            @PathVariable("groupId") Integer conversationId,
            @Valid @RequestBody AddMemberRequestDTO request) {

        conversationService.addMembersToGroup(conversationId, request);

        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.value(),
                "Thêm thành viên thành công!",
                null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ConversationSummaryDTO>>> getGroupConversations() {
        List<ConversationSummaryDTO> groups = conversationService.getGroupConversationsForCurrentUser();
        ApiResponse<List<ConversationSummaryDTO>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Lấy danh sách nhóm thành công",
                groups
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/{groupId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ConversationSummaryDTO>> updateGroupInfo(
            @PathVariable Integer groupId,
            @ModelAttribute UpdateGroupRequestDTO request) {
        ConversationSummaryDTO updatedGroup = conversationService.updateGroupInfo(groupId, request);
        ApiResponse<ConversationSummaryDTO> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Cập nhật thông tin nhóm thành công!",
                updatedGroup
        );
        return ResponseEntity.ok(response);
    }


}
