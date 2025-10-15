package com.example.project_chat.repository;

import com.example.project_chat.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Integer> {
    Optional<Message> findFirstByConversationIdOrderByCreatedAtDesc(Integer conversationId);
    Page<Message> findByConversationIdOrderByCreatedAtDesc(Integer conversationId, Pageable pageable);

    @Query("SELECT m FROM Message m " +
            "WHERE m.conversationId = :conversationId " +
            "AND m.id <= :lastMessageId " +
            "AND m.senderId != :userId " +
            "AND NOT EXISTS (SELECT 1 FROM MessageRead mr WHERE mr.messageId = m.id AND mr.userId = :userId)")
    List<Message> findMessagesToMarkAsRead(@Param("conversationId") Integer conversationId,
                                           @Param("userId") Integer userId,
                                           @Param("lastMessageId") Integer lastMessageId);

    Page<Message> findByConversationIdAndContentContainingIgnoreCaseOrderByCreatedAtDesc(Integer conversationId,
                                                                                         String keyword,
                                                                                         Pageable pageable);
    Page<Message> findByConversationIdAndCreatedAtAfterOrderByCreatedAtDesc(Integer conversationId, Timestamp after, Pageable pageable);
}
