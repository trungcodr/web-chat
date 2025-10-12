package com.example.project_chat.dto.group;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class CreateGroupRequestDTO {
    @NotNull(message = "Ten nhom khong duoc de trong")
    private String groupName;

    @NotEmpty(message = "Phai moi it nhat 2 thanh vien")
    @Size(min = 2,message = "Phai moi it nhat 2 thanh vien")
    private List<Integer> memberIds;

    private MultipartFile avatarFile;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<Integer> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(List<Integer> memberIds) {
        this.memberIds = memberIds;
    }

    public MultipartFile getAvatarFile() {
        return avatarFile;
    }

    public void setAvatarFile(MultipartFile avatarFile) {
        this.avatarFile = avatarFile;
    }
}
