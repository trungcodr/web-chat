package com.example.project_chat.dto.search;

import com.example.project_chat.dto.friend.FriendResponseDTO;
import com.example.project_chat.dto.message.ConversationSummaryDTO;

import java.util.List;

public class SearchResultDTO {
    private List<FriendResponseDTO> friends;
    private List<ConversationSummaryDTO> groups;

    public List<FriendResponseDTO> getFriends() {
        return friends;
    }

    public void setFriends(List<FriendResponseDTO> friends) {
        this.friends = friends;
    }

    public List<ConversationSummaryDTO> getGroups() {
        return groups;
    }

    public void setGroups(List<ConversationSummaryDTO> groups) {
        this.groups = groups;
    }
}
