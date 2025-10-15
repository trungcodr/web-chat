package com.example.project_chat.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "user_conversation_clears")
public class UserConversationClear {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "conversation_id", nullable = false)
    private Integer conversationId;

    @Column(name = "cleared_at", nullable = false)
    private Timestamp clearedAt;

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

    public Timestamp getClearedAt() {
        return clearedAt;
    }

    public void setClearedAt(Timestamp clearedAt) {
        this.clearedAt = clearedAt;
    }
}
