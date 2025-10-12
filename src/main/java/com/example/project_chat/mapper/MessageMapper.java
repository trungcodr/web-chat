package com.example.project_chat.mapper;

import com.example.project_chat.dto.message.MessageResponseDTO;
import com.example.project_chat.dto.message.ReplyInfoDTO;
import com.example.project_chat.entity.Message;
import com.example.project_chat.entity.User;
import com.example.project_chat.repository.MessageRepository;
import com.example.project_chat.repository.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class MessageMapper {
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    public MessageMapper(UserRepository userRepository, MessageRepository messageRepository) {
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
    }

    public MessageResponseDTO toMessageResponseDTO(Message message) {
        if (message == null) {
            return null;
        }

        MessageResponseDTO dto = new MessageResponseDTO();
        dto.setId(message.getId());
        dto.setConversationId(message.getConversationId());
        dto.setType(message.getType());
        dto.setContent(message.getContent());
        dto.setFileUrl(message.getFileUrl());
        dto.setFileName(message.getFileName());
        dto.setFileSize(message.getFileSize());
        dto.setStickerId(message.getStickerId());
        dto.setLatitude(message.getLatitude());
        dto.setLongitude(message.getLongitude());
        dto.setReplyToId(message.getReplyToId());
        dto.setCreatedAt(message.getCreatedAt());
        dto.setStatus(message.getStatus());

        if (message.getSenderId() != null) {
            User sender = userRepository.findById(message.getSenderId()).orElse(null);
            dto.setSender(UserMapper.toUserResponseDTO(sender));
        }

        // neu day la tin nhan tra loi, lay thong tin cua tin nhan goc
        if (message.getReplyToId() != null) {
            messageRepository.findById(message.getReplyToId()).ifPresent(repliedMessage -> {
                ReplyInfoDTO replyInfo = new ReplyInfoDTO();
                replyInfo.setMessageId(repliedMessage.getId());
                replyInfo.setMessageType(repliedMessage.getType());

                // lay ten nguoi gui cua tin nhan goc
                userRepository.findById(repliedMessage.getSenderId()).ifPresent(repliedSender -> {
                    replyInfo.setSenderName(repliedSender.getDisplayName());
                });

                // tao noi dung tom tat
                switch (repliedMessage.getType()) {
                    case IMAGE:
                        replyInfo.setContent("Ảnh");
                        break;
                    case FILE:
                        replyInfo.setContent("Tệp đính kèm");
                        break;
                    // ... các trường hợp khác
                    default:
                        replyInfo.setContent(repliedMessage.getContent());
                }

                dto.setReplyInfo(replyInfo);
            });
        }
        return dto;
    }
}
