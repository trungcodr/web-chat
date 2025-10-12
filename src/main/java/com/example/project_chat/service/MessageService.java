package com.example.project_chat.service;

import com.example.project_chat.dto.message.EditMessageRequestDTO;
import com.example.project_chat.dto.message.MessageResponseDTO;
import com.example.project_chat.dto.message.SendMessageRequestDTO;
import com.example.project_chat.dto.response.ConversationHistoryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MessageService {
    MessageResponseDTO sendMessage(SendMessageRequestDTO sendMessageRequestDTO);
    ConversationHistoryDTO getMessagesByConversationId(Integer conversationId, Pageable pageable);
    void markConversationAsRead(Integer conversationId,Integer lastMessageId);
    Page<MessageResponseDTO> searchMessagesInConversation(Integer conversationId, String keyword, Pageable pageable);
    MessageResponseDTO editMessage(EditMessageRequestDTO editMessageRequestDTO);
}
