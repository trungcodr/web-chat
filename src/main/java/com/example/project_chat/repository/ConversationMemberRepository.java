package com.example.project_chat.repository;

import com.example.project_chat.entity.ConversationMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConversationMemberRepository extends JpaRepository<ConversationMember, Integer> {
    List<ConversationMember> findByUserId(Integer userId);
    List<ConversationMember> findByConversationId(Integer conversationId);
    boolean existsByConversationIdAndUserId(Integer conversationId, Integer userId);
}
