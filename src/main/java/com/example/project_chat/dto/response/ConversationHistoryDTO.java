package com.example.project_chat.dto.response;

import com.example.project_chat.dto.message.MessageResponseDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public class ConversationHistoryDTO {
    private Page<MessageResponseDTO> messages;
    private List<UserResponseDTO> members;

    public Page<MessageResponseDTO> getMessages() {
        return messages;
    }

    public void setMessages(Page<MessageResponseDTO> messages) {
        this.messages = messages;
    }

    public List<UserResponseDTO> getMembers() {
        return members;
    }

    public void setMembers(List<UserResponseDTO> members) {
        this.members = members;
    }
}
