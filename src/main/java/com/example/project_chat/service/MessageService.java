package com.example.project_chat.service;

import com.example.project_chat.dto.message.MessageResponseDTO;
import com.example.project_chat.dto.message.SendMessageRequestDTO;

public interface MessageService {
    MessageResponseDTO sendMessage(SendMessageRequestDTO sendMessageRequestDTO);
}
