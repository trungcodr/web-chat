package com.example.project_chat.entity;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "message_reads")
public class MessageRead {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer user_id;


    @Column(name = "message_id", nullable = false)
    private Integer messageId;

    @Column(name = "read_at", updatable = false)
    private Timestamp readAt;

    public MessageRead() {
    }

    public MessageRead(Integer id, Integer user_id, Integer messageId, Timestamp readAt) {
        this.id = id;
        this.user_id = user_id;
        this.messageId = messageId;
        this.readAt = readAt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUser_id() {
        return user_id;
    }

    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }

    public Integer getMessageId() {
        return messageId;
    }

    public void setMessageId(Integer messageId) {
        this.messageId = messageId;
    }

    public Timestamp getReadAt() {
        return readAt;
    }

    public void setReadAt(Timestamp readAt) {
        this.readAt = readAt;
    }
}
