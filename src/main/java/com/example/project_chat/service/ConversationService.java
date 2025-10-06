package com.example.project_chat.service;

import com.example.project_chat.dto.message.ConversationSummaryDTO;
import com.example.project_chat.entity.Conversation;

import java.util.List;

public interface ConversationService {
    Conversation findOrCreateConversation(Integer user1Id, Integer user2Id);
    List<ConversationSummaryDTO> getConversationsForCurrentUser();
}
