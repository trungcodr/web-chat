package com.example.project_chat.dto.group;

import org.springframework.web.multipart.MultipartFile;

public class UpdateGroupRequestDTO {
    private String name;
    private MultipartFile avatarFile;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MultipartFile getAvatarFile() {
        return avatarFile;
    }

    public void setAvatarFile(MultipartFile avatarFile) {
        this.avatarFile = avatarFile;
    }
}
