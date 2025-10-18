package com.example.project_chat.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;


@Entity
@Table(name = "notification_settings")
public class NotificationSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "user_id", nullable = false)
    private Integer userId;
    @Column(name = "conversation_id")
    private Integer conversationId;
    @Column(name = "enable_notifications")
    private boolean enableNotifications = true;
    @Column(name = "only_mentions")
    private boolean onlyMentions = false;
    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt;
    @Column(name = "updated_at")
    private Timestamp updatedAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getConversationId() {
        return conversationId;
    }

    public void setConversationId(Integer conversationId) {
        this.conversationId = conversationId;
    }

    public boolean isEnableNotifications() {
        return enableNotifications;
    }

    public void setEnableNotifications(boolean enableNotifications) {
        this.enableNotifications = enableNotifications;
    }

    public boolean isOnlyMentions() {
        return onlyMentions;
    }

    public void setOnlyMentions(boolean onlyMentions) {
        this.onlyMentions = onlyMentions;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}
