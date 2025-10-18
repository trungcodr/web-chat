package com.example.project_chat.entity;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "pinned_messages")
public class PinnedMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "conversation_id" , nullable = false)
    private Integer conversationId;
    @Column(name = "message_id", nullable = false)
    private Integer messageId;
    @Column(name = "pinned_by")
    private Integer pinnedBy;
    @Column(name = "pinned_at", updatable = false)
    private Timestamp pinnedAt;

    public PinnedMessage() {
    }

    public PinnedMessage(Integer id, Integer conversationId, Integer messageId, Integer pinnedBy, Timestamp pinnedAt) {
        this.id = id;
        this.conversationId = conversationId;
        this.messageId = messageId;
        this.pinnedBy = pinnedBy;
        this.pinnedAt = pinnedAt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getConversationId() {
        return conversationId;
    }

    public void setConversationId(Integer conversationId) {
        this.conversationId = conversationId;
    }

    public Integer getMessageId() {
        return messageId;
    }

    public void setMessageId(Integer messageId) {
        this.messageId = messageId;
    }

    public Integer getPinnedBy() {
        return pinnedBy;
    }

    public void setPinnedBy(Integer pinnedBy) {
        this.pinnedBy = pinnedBy;
    }

    public Timestamp getPinnedAt() {
        return pinnedAt;
    }

    public void setPinnedAt(Timestamp pinnedAt) {
        this.pinnedAt = pinnedAt;
    }
}
