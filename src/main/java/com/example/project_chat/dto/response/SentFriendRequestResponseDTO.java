package com.example.project_chat.dto.response;

public class SentFriendRequestResponseDTO {
    private Integer friendRequestId;
    private Integer receiverId;
    private String receiverDisplayName;
    private String receiverAvatarUrl;

    public Integer getFriendRequestId() {
        return friendRequestId;
    }

    public void setFriendRequestId(Integer friendRequestId) {
        this.friendRequestId = friendRequestId;
    }

    public Integer getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Integer receiverId) {
        this.receiverId = receiverId;
    }

    public String getReceiverDisplayName() {
        return receiverDisplayName;
    }

    public void setReceiverDisplayName(String receiverDisplayName) {
        this.receiverDisplayName = receiverDisplayName;
    }

    public String getReceiverAvatarUrl() {
        return receiverAvatarUrl;
    }

    public void setReceiverAvatarUrl(String receiverAvatarUrl) {
        this.receiverAvatarUrl = receiverAvatarUrl;
    }
}
