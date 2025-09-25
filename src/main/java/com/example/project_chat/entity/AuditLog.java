package com.example.project_chat.entity;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id") // Cho phép NULL
    private Integer userId;

    @Column(length = 100, nullable = false)
    private String action;

    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt;

    public AuditLog() {
    }

    public AuditLog(Integer id, Integer userId, String action, Timestamp createdAt) {
        this.id = id;
        this.userId = userId;
        this.action = action;
        this.createdAt = createdAt;
    }

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

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
