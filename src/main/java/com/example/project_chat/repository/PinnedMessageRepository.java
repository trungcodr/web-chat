package com.example.project_chat.repository;

import com.example.project_chat.entity.PinnedMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PinnedMessageRepository extends JpaRepository<PinnedMessage, Integer> {
    List<PinnedMessage> findByConversationIdOrderByPinnedAtDesc(Integer conversationId);
    boolean existsByConversationIdAndMessageId(Integer conversationId, Integer messageId);
    Optional<PinnedMessage> findByConversationIdAndMessageId(Integer conversationId, Integer messageId);
}
