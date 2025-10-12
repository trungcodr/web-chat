package com.example.project_chat.dto.message;

import com.example.project_chat.common.constants.MessageType;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

public class SendMessageRequestDTO {
    //Id cua nguoi nhan (trong chat 1 -1)
//    @NotNull(message = "Id nguoi nhan khong duoc de trong!")
    private Integer receiverId;
    private Integer conversationId;
    //Loai tin nhan
    @NotNull(message = "Loai tin nhan khong duoc de trong")
    private MessageType type;

    //Noi dung tin nhan
    private String content;
    //file
    private MultipartFile file;
    private Integer stickerId;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer replyToId;

    public Integer getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Integer receiverId) {
        this.receiverId = receiverId;
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

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
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

    public Integer getConversationId() {
        return conversationId;
    }

    public void setConversationId(Integer conversationId) {
        this.conversationId = conversationId;
    }
}
