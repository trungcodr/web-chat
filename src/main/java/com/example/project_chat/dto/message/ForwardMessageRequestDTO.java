package com.example.project_chat.dto.message;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class ForwardMessageRequestDTO {
    @NotNull(message = "Id tin nhan khong duoc de trong")
    private Integer messageId;
    @NotEmpty(message = "Danh sach Id cuoc tro chuyen khong duoc de trong")
    private List<Integer> conversationIds;

    public Integer getMessageId() {
        return messageId;
    }

    public void setMessageId(Integer messageId) {
        this.messageId = messageId;
    }

    public List<Integer> getConversationIds() {
        return conversationIds;
    }

    public void setConversationIds(List<Integer> conversationIds) {
        this.conversationIds = conversationIds;
    }
}
