package com.example.project_chat.controller;

import com.example.project_chat.dto.message.EditMessageRequestDTO;
import com.example.project_chat.dto.message.MessageResponseDTO;
import com.example.project_chat.dto.message.SendMessageRequestDTO;
import com.example.project_chat.dto.response.ApiResponse;
import com.example.project_chat.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
public class MessageController {
    private final MessageService messageService;
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<MessageResponseDTO>> sendMessage(@Valid @ModelAttribute SendMessageRequestDTO request) {
        MessageResponseDTO newMessage = messageService.sendMessage(request);
        ApiResponse<MessageResponseDTO> response = new ApiResponse<>(
                HttpStatus.CREATED.value(),
                "Gửi tin nhắn thành công!",
                newMessage
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<ApiResponse<MessageResponseDTO>> editMessage(@Valid @RequestBody EditMessageRequestDTO request) {
        MessageResponseDTO editedMessage = messageService.editMessage(request);
        ApiResponse<MessageResponseDTO> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Chinh sua tin nhan thanh cong!",
                editedMessage
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
