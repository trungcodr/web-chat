package com.example.project_chat.dto.friend;

import jakarta.validation.constraints.NotBlank;

public class FriendRequestDTO {
    @NotBlank(message = "Ten hien thi khong duoc de trong!")
    private String displayName;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
