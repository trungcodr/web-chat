package com.example.project_chat.dto.message;

import com.example.project_chat.common.constants.MessageType;

import java.util.Date;

public class LastMessageDTO {
    private String content;
    private MessageType type;
    private Date createdAt;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
