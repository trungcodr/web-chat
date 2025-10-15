package com.example.project_chat.repository;

import com.example.project_chat.entity.UserConversationClear;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserConversationClearRepository extends JpaRepository<UserConversationClear, Integer> {
    Optional<UserConversationClear> findByUserIdAndConversationId(Integer userId, Integer conversationId);
}
