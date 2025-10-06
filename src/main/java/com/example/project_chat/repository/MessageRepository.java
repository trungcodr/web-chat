package com.example.project_chat.repository;

import com.example.project_chat.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Integer> {
    Optional<Message> findFirstByConversationIdOrderByCreatedAtDesc(Integer conversationId);
    Page<Message> findByConversationIdOrderByCreatedAtDesc(Integer conversationId, Pageable pageable);
}
