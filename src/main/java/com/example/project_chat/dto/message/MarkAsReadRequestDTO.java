package com.example.project_chat.dto.message;

public class MarkAsReadRequestDTO {
    private Integer lastMessageId;

    public Integer getLastMessageId() {
        return lastMessageId;
    }

    public void setLastMessageId(Integer lastMessageId) {
        this.lastMessageId = lastMessageId;
    }
}
