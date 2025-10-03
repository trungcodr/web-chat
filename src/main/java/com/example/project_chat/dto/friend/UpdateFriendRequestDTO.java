package com.example.project_chat.dto.friend;

import com.example.project_chat.common.constants.FriendStatus;

import jakarta.validation.constraints.NotNull;

public class UpdateFriendRequestDTO {
    //dung cho ca accept hoac reject
    @NotNull(message = "Trang thai khong duoc de trong")
    private FriendStatus status;

    public FriendStatus getStatus() {
        return status;
    }

    public void setStatus(FriendStatus status) {
        this.status = status;
    }
}
