package com.example.project_chat.dto.group;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class AddMemberRequestDTO {
    @NotEmpty(message = "Danh sach thanh vien khong duoc de trong")
    private List<Integer> memberIds;

    public List<Integer> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(List<Integer> memberIds) {
        this.memberIds = memberIds;
    }
}
