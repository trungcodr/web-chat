package com.example.project_chat.controller;

import com.example.project_chat.dto.message.ConversationSummaryDTO;
import com.example.project_chat.dto.message.MarkAsReadRequestDTO;
import com.example.project_chat.dto.message.MessageResponseDTO;
import com.example.project_chat.dto.notification.UpdateNotificationSettingsDTO;
import com.example.project_chat.dto.response.ApiResponse;
import com.example.project_chat.dto.response.ConversationHistoryDTO;
import com.example.project_chat.service.ConversationService;
import com.example.project_chat.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {
    private final ConversationService conversationService;
    private final MessageService messageService;
    public ConversationController(ConversationService conversationService, MessageService messageService) {
        this.conversationService = conversationService;
        this.messageService = messageService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ConversationSummaryDTO>>> getConversations() {
        List<ConversationSummaryDTO> conversationSummaryDTOS = conversationService.getConversationsForCurrentUser();
        ApiResponse<List<ConversationSummaryDTO>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Lấy danh sách cuộc trò chuyện thành công!",
                conversationSummaryDTOS
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<ApiResponse<ConversationHistoryDTO>> getMessagesByConversationId(
            @PathVariable Integer conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        ConversationHistoryDTO history = messageService.getMessagesByConversationId(conversationId, pageable);

        ApiResponse<ConversationHistoryDTO> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Lấy lịch sử tin nhắn thành công!",
                history
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("{conversationId}/read")
    public ResponseEntity<ApiResponse<?>> markConversationAsRead(
            @PathVariable Integer conversationId,
            @Valid @RequestBody MarkAsReadRequestDTO requestDTO) {
        messageService.markConversationAsRead(conversationId, requestDTO.getLastMessageId());
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.value(),
                "Đánh dấu là đã đọc thành công!",
                null));
    }

    @GetMapping("/{conversationId}/search")
    public ResponseEntity<ApiResponse<Page<MessageResponseDTO>>> searchMessagesInConversation(
            @PathVariable Integer conversationId,
            @RequestParam("q") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<MessageResponseDTO> result = messageService.searchMessagesInConversation(conversationId, keyword, pageable);

        ApiResponse<Page<MessageResponseDTO>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Tìm kiếm tin nhắn thành công!",
                result
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{conversationId}/messages")
    public ResponseEntity<ApiResponse<?>> clearHistory(@PathVariable Integer conversationId) {
        messageService.clearHistoryForCurrentUser(conversationId);
        ApiResponse<?> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Xóa lịch sử cuộc trò chuyện thành công!",
                null
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{conversationId}/pinned")
    public ResponseEntity<ApiResponse<List<MessageResponseDTO>>> getPinnedMessages(@PathVariable Integer conversationId) {
        List<MessageResponseDTO> pinnedMessages = messageService.getPinnedMessages(conversationId);
        ApiResponse<List<MessageResponseDTO>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Lay danh sach tin nhan da ghim thanh cong!",
                pinnedMessages
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{conversationId}/notifications")
    public ResponseEntity<ApiResponse<?>> updateNotificationSettings(
            @PathVariable Integer conversationId,
            @Valid @RequestBody UpdateNotificationSettingsDTO request) {
        conversationService.updateNotificationSettings(conversationId, request);
        String message = request.getEnableNotifications()
                ? "Da bat thong bao cho cuoc tro chuyen."
                : "Da tat thong bao cho cuoc tro chuyen.";

        ApiResponse<?> response = new ApiResponse<>(HttpStatus.OK.value(), message, null);
        return ResponseEntity.ok(response);
    }
}
