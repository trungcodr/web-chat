package com.example.project_chat.repository;

import com.example.project_chat.entity.MessageRead;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageReadRepository extends JpaRepository<MessageRead, Integer> {
}
