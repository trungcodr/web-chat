package com.example.project_chat.service;

import com.example.project_chat.entity.Conversation;

public interface ConversationService {
    Conversation findOrCreateConversation(Integer user1Id, Integer user2Id);
}
