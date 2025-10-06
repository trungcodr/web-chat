package com.example.project_chat.dto.message;

import java.util.List;

public class ConversationSummaryDTO {
    private Integer id; //conversationId
    private String name;
    private String avatarUrl;
    private List<Integer> members;
    private LastMessageDTO lastMessage;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public List<Integer> getMembers() {
        return members;
    }

    public void setMembers(List<Integer> members) {
        this.members = members;
    }

    public LastMessageDTO getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(LastMessageDTO lastMessage) {
        this.lastMessage = lastMessage;
    }
}
