package com.example.project_chat.dto.notification;

public class UserStatusNotificationDTO {
    private Integer userId;
    private boolean online;

    public UserStatusNotificationDTO(Integer userId, boolean online) {
        this.userId = userId;
        this.online = online;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }
}
