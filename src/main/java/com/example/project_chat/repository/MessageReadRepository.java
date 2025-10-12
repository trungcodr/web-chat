package com.example.project_chat.repository;

import com.example.project_chat.entity.MessageRead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageReadRepository extends JpaRepository<MessageRead, Integer> {
    boolean existsByMessageIdAndUserId(Integer messageId, Integer userId);
}
