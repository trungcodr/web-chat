package com.example.project_chat.repository;

import com.example.project_chat.entity.ConversationMember;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ConversationMemberRepository extends JpaRepository<ConversationMember, Integer> {
    List<ConversationMember> findByUserId(Integer userId);
    List<ConversationMember> findByConversationId(Integer conversationId);
    boolean existsByConversationIdAndUserId(Integer conversationId, Integer userId);
    List<ConversationMember> findByConversationIdAndUserIdIn(Integer conversationId, List<Integer> userIds);
    Optional<ConversationMember> findByConversationIdAndUserId(Integer conversationId, Integer userId);
}
