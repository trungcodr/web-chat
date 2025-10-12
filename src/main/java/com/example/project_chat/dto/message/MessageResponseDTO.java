package com.example.project_chat.dto.message;

import com.example.project_chat.common.constants.MessageStatus;
import com.example.project_chat.common.constants.MessageType;
import com.example.project_chat.dto.response.UserResponseDTO;

import java.math.BigDecimal;
import java.util.Date;

public class MessageResponseDTO {
    private Integer id;
    private Integer conversationId;
    private UserResponseDTO sender; // Tái sử dụng UserResponseDTO để hiển thị thông tin người gửi
    private MessageType type;
    private String content;
    private String fileUrl;
    private String fileName;
    private Integer fileSize;
    private Integer stickerId;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer replyToId;
    private ReplyInfoDTO replyInfo;
    private Date createdAt;
    private MessageStatus status;
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getConversationId() {
        return conversationId;
    }

    public void setConversationId(Integer conversationId) {
        this.conversationId = conversationId;
    }

    public UserResponseDTO getSender() {
        return sender;
    }

    public void setSender(UserResponseDTO sender) {
        this.sender = sender;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Integer getFileSize() {
        return fileSize;
    }

    public void setFileSize(Integer fileSize) {
        this.fileSize = fileSize;
    }

    public Integer getStickerId() {
        return stickerId;
    }

    public void setStickerId(Integer stickerId) {
        this.stickerId = stickerId;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public Integer getReplyToId() {
        return replyToId;
    }

    public void setReplyToId(Integer replyToId) {
        this.replyToId = replyToId;
    }

    public ReplyInfoDTO getReplyInfo() {
        return replyInfo;
    }

    public void setReplyInfo(ReplyInfoDTO replyInfo) {
        this.replyInfo = replyInfo;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public void setStatus(MessageStatus status) {
        this.status = status;
    }
}
