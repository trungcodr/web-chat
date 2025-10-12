package com.example.project_chat.service;

import com.example.project_chat.dto.group.AddMemberRequestDTO;
import com.example.project_chat.dto.group.CreateGroupRequestDTO;
import com.example.project_chat.dto.group.UpdateGroupRequestDTO;
import com.example.project_chat.dto.message.ConversationSummaryDTO;
import com.example.project_chat.entity.Conversation;

import java.util.List;

public interface ConversationService {
    Conversation findOrCreateConversation(Integer user1Id, Integer user2Id);
    List<ConversationSummaryDTO> getConversationsForCurrentUser();
    ConversationSummaryDTO createGroupConversation(CreateGroupRequestDTO requestDTO);
    void addMembersToGroup(Integer conversationId, AddMemberRequestDTO requestDTO);
    List<ConversationSummaryDTO> getGroupConversationsForCurrentUser();
    ConversationSummaryDTO updateGroupInfo(Integer groupId, UpdateGroupRequestDTO requestDTO);
}
