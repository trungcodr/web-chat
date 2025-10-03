package com.example.project_chat.mapper;

import com.example.project_chat.dto.message.MessageResponseDTO;
import com.example.project_chat.entity.Message;
import com.example.project_chat.entity.User;
import com.example.project_chat.repository.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class MessageMapper {
    private final UserRepository userRepository;

    public MessageMapper(UserRepository userRepository) {
        this.userRepository = userRepository;
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

        // Lấy thông tin người gửi và map sang DTO
        if (message.getSenderId() != null) {
            User sender = userRepository.findById(message.getSenderId()).orElse(null);
            dto.setSender(UserMapper.toUserResponseDTO(sender));
        }

        return dto;
    }
}
