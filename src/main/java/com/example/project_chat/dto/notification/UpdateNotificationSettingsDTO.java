package com.example.project_chat.dto.notification;

import jakarta.validation.constraints.NotNull;

public class UpdateNotificationSettingsDTO {
    @NotNull(message = "Trạng thái thông báo không được để trống")
    private Boolean enableNotifications;

    private Boolean onlyMentions;

    public Boolean getEnableNotifications() {
        return enableNotifications;
    }

    public void setEnableNotifications(Boolean enableNotifications) {
        this.enableNotifications = enableNotifications;
    }

    public Boolean getOnlyMentions() {
        return onlyMentions;
    }

    public void setOnlyMentions(Boolean onlyMentions) {
        this.onlyMentions = onlyMentions;
    }
}
