package com.example.project_chat.repository;

import com.example.project_chat.entity.NotificationSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationSettingsRepository extends JpaRepository<NotificationSettings, Integer> {
    Optional<NotificationSettings> findByUserIdAndConversationId(Integer userId, Integer conversationId);
}
