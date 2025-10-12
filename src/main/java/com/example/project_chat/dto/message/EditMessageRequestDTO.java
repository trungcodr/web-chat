package com.example.project_chat.dto.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class EditMessageRequestDTO {
    @NotNull(message = "Id khong duoc de trong")
    private Integer messageId;
    @NotBlank(message = "Noi dung khong duoc de trong")
    private String newContent;

    public Integer getMessageId() {
        return messageId;
    }

    public void setMessageId(Integer messageId) {
        this.messageId = messageId;
    }

    public String getNewContent() {
        return newContent;
    }

    public void setNewContent(String newContent) {
        this.newContent = newContent;
    }
}
