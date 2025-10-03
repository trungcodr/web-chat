package com.example.project_chat.service.impl;

import com.example.project_chat.common.exception.BadRequestException;
import com.example.project_chat.common.exception.ResourceNotFoundException;
import com.example.project_chat.dto.message.MessageResponseDTO;
import com.example.project_chat.dto.message.SendMessageRequestDTO;
import com.example.project_chat.entity.Conversation;
import com.example.project_chat.entity.Message;
import com.example.project_chat.entity.User;
import com.example.project_chat.mapper.MessageMapper;
import com.example.project_chat.repository.MessageRepository;
import com.example.project_chat.repository.UserRepository;
import com.example.project_chat.service.ConversationService;
import com.example.project_chat.service.FileStorageService;
import com.example.project_chat.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MessageServiceImpl implements MessageService {
    private static final Logger log = LoggerFactory.getLogger(MessageServiceImpl.class);
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ConversationService conversationService;
    private final FileStorageService fileStorageService;
    private final MessageMapper messageMapper;

    public MessageServiceImpl(MessageRepository messageRepository, UserRepository userRepository, ConversationService conversationService, FileStorageService fileStorageService, MessageMapper messageMapper) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.conversationService = conversationService;
        this.fileStorageService = fileStorageService;
        this.messageMapper = messageMapper;
    }

    @Override
    @Transactional
    public MessageResponseDTO sendMessage(SendMessageRequestDTO requestDTO) {
        //lay thong tin nguoi gui
        String senderEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay nguoi gui!"));
        //lay thong tin nguoi nhan
        User receiver = userRepository.findById(requestDTO.getReceiverId())
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay nguoi nhan!"));
        //tim hoac tao cuoc tro chuyen 1-1
        Conversation conversation = conversationService.findOrCreateConversation(sender.getId(), receiver.getId());
        //Tao doi tuong message
        Message message = new Message();
        message.setConversationId(conversation.getId());
        message.setSenderId(sender.getId());
        message.setType(requestDTO.getType());
        message.setReplyToId(requestDTO.getReplyToId());
        //xu ly loai tin nhan text , image ,file,....
        switch (requestDTO.getType()) {
            case TEXT:
                if (requestDTO.getContent() == null || requestDTO.getContent().isBlank()) {
                    throw new BadRequestException("Noi dung tin nhan van ban khong duoc de trong!");
                }
                message.setContent(requestDTO.getContent());
                break;
            case IMAGE:
            case FILE:
            case VOICE:
                MultipartFile file = requestDTO.getFile();
                if (file == null || file.isEmpty()) {
                    throw new BadRequestException("File khong duoc de trong!.");
                }
                String fileUrl = fileStorageService.uploadFile(file);
                message.setFileUrl(fileUrl);
                message.setFileName(file.getOriginalFilename());
                message.setFileSize((int) file.getSize());
                break;
            case STICKER:
            case LOCATION:
        }
        //luu tin nhan vao csdl
        Message savedMessage = messageRepository.save(message);
        log.info("Da luu tin nhan ID {} vao cuoc tro chuyen ID{}", savedMessage.getId(), conversation.getId());
        return messageMapper.toMessageResponseDTO(savedMessage);
    }
}
