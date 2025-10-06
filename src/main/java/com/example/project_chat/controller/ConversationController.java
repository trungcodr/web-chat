package com.example.project_chat.controller;

import com.example.project_chat.dto.message.ConversationSummaryDTO;
import com.example.project_chat.dto.response.ApiResponse;
import com.example.project_chat.dto.response.ConversationHistoryDTO;
import com.example.project_chat.service.ConversationService;
import com.example.project_chat.service.MessageService;
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
                "Lay danh sach cuoc tro chuyen thanh cong!",
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
                "Lay lich su tin nhan thanh cong!",
                history
        );
        return ResponseEntity.ok(response);
    }
}
