package com.example.project_chat.service.impl;

import com.example.project_chat.common.exception.BadRequestException;
import com.example.project_chat.common.exception.ResourceNotFoundException;
import com.example.project_chat.dto.message.MessageResponseDTO;
import com.example.project_chat.dto.message.SendMessageRequestDTO;
import com.example.project_chat.dto.response.ConversationHistoryDTO;
import com.example.project_chat.dto.response.UserResponseDTO;
import com.example.project_chat.entity.Conversation;
import com.example.project_chat.entity.ConversationMember;
import com.example.project_chat.entity.Message;
import com.example.project_chat.entity.User;
import com.example.project_chat.mapper.MessageMapper;
import com.example.project_chat.mapper.UserMapper;
import com.example.project_chat.repository.ConversationMemberRepository;
import com.example.project_chat.repository.MessageRepository;
import com.example.project_chat.repository.UserRepository;
import com.example.project_chat.service.ConversationService;
import com.example.project_chat.service.FileStorageService;
import com.example.project_chat.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageServiceImpl implements MessageService {
    private static final Logger log = LoggerFactory.getLogger(MessageServiceImpl.class);
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ConversationService conversationService;
    private final FileStorageService fileStorageService;
    private final MessageMapper messageMapper;
    private final ConversationMemberRepository conversationMemberRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public MessageServiceImpl(MessageRepository messageRepository, UserRepository userRepository, ConversationService conversationService, FileStorageService fileStorageService, MessageMapper messageMapper, ConversationMemberRepository conversationMemberRepository, SimpMessagingTemplate messagingTemplate) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.conversationService = conversationService;
        this.fileStorageService = fileStorageService;
        this.messageMapper = messageMapper;
        this.conversationMemberRepository = conversationMemberRepository;
        this.messagingTemplate = messagingTemplate;
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
        message.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        Message savedMessage = messageRepository.save(message);
        log.info("Da luu tin nhan ID {} vao cuoc tro chuyen ID {}", savedMessage.getId(), conversation.getId());

        //chuyen doi tin nhan da luu sang dto
        MessageResponseDTO messageResponseDTO = messageMapper.toMessageResponseDTO(savedMessage);
        //gui tin nhan websocket den kenh cua cuoc tro chuyen
        String destination = "/topic/conversations/" + conversation.getId();
        messagingTemplate.convertAndSend(destination, messageResponseDTO);
        log.info("Da day tin nhan ID {} den WebSocket topic : {}", savedMessage.getId(), destination);

        return messageResponseDTO;
    }

    @Override
    public ConversationHistoryDTO getMessagesByConversationId(Integer conversationId, Pageable pageable) {
        //Lay thong tin nguoi dung hien tai
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay nguoi dung hien tai!"));
        boolean isMember = conversationMemberRepository.existsByConversationIdAndUserId(conversationId, currentUser.getId());
        if (!isMember) {
            throw new BadRequestException("Ban khong co quyen truy cap vao cuoc tro chuyen nay!");
        }
        Page<Message> messagePage = messageRepository.findByConversationIdOrderByCreatedAtDesc(conversationId, pageable);
        Page<MessageResponseDTO> messageDtoPage = messagePage.map(messageMapper::toMessageResponseDTO);

        List<ConversationMember> members = conversationMemberRepository.findByConversationId(conversationId);
        List<Integer> memberIds = members.stream().map(ConversationMember::getUserId).collect(Collectors.toList());
        List<User> memberUsers = userRepository.findAllById(memberIds);
        List<UserResponseDTO> memberDtos = memberUsers.stream()
                .map(UserMapper::toUserResponseDTO)
                .collect(Collectors.toList());

        ConversationHistoryDTO historyDTO = new ConversationHistoryDTO();
        historyDTO.setMessages(messageDtoPage);
        historyDTO.setMembers(memberDtos);

        return historyDTO;
    }


}
