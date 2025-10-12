package com.example.project_chat.service.impl;

import com.example.project_chat.common.constants.MessageStatus;
import com.example.project_chat.common.constants.MessageType;
import com.example.project_chat.common.exception.BadRequestException;
import com.example.project_chat.common.exception.ResourceNotFoundException;
import com.example.project_chat.dto.message.EditMessageRequestDTO;
import com.example.project_chat.dto.message.MessageResponseDTO;
import com.example.project_chat.dto.message.SendMessageRequestDTO;
import com.example.project_chat.dto.response.ConversationHistoryDTO;
import com.example.project_chat.dto.response.UserResponseDTO;
import com.example.project_chat.entity.*;
import com.example.project_chat.mapper.MessageMapper;
import com.example.project_chat.mapper.UserMapper;
import com.example.project_chat.repository.*;
import com.example.project_chat.service.ConversationService;
import com.example.project_chat.service.FileStorageService;
import com.example.project_chat.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageServiceImpl implements MessageService {
    private static final Logger log = LoggerFactory.getLogger(MessageServiceImpl.class);
    private final MessageRepository messageRepository;
    private final MessageReadRepository readRepository;
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationService conversationService;
    private final FileStorageService fileStorageService;
    private final MessageMapper messageMapper;
    private final ConversationMemberRepository conversationMemberRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public MessageServiceImpl(MessageRepository messageRepository, MessageReadRepository readRepository, UserRepository userRepository, ConversationRepository conversationRepository, ConversationService conversationService, FileStorageService fileStorageService, MessageMapper messageMapper, ConversationMemberRepository conversationMemberRepository, SimpMessagingTemplate messagingTemplate) {
        this.messageRepository = messageRepository;
        this.readRepository = readRepository;
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
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

        Conversation conversation;
        if (requestDTO.getConversationId() != null) {
            boolean isMember = conversationMemberRepository.existsByConversationIdAndUserId(requestDTO.getConversationId(), sender.getId());
            if (!isMember) {
                throw new BadRequestException("Ban khong phai thanh vien cua cuoc tro chuyen nay!");
            }
            conversation = conversationRepository.findById(requestDTO.getConversationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cuộc trò chuyện!"));
        }
        else if (requestDTO.getReceiverId() != null) {
            User receiver = userRepository.findById(requestDTO.getReceiverId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người nhận!"));
            conversation = conversationService.findOrCreateConversation(sender.getId(), receiver.getId());
        } else {
            throw new BadRequestException("Yêu cầu gửi tin nhắn không hợp lệ (thiếu conversationId hoặc receiverId).");
        }
        //Tao doi tuong message
        Message message = new Message();
        message.setConversationId(conversation.getId());
        message.setSenderId(sender.getId());
        message.setType(requestDTO.getType());
        // Kiểm tra và gán replyToId
        if (requestDTO.getReplyToId() != null) {
            // Kiểm tra xem tin nhắn được trả lời có tồn tại và thuộc cùng cuộc trò chuyện không
            Message repliedMessage = messageRepository.findById(requestDTO.getReplyToId())
                    .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay tin nhan duoc tra loi."));

            if (!repliedMessage.getConversationId().equals(conversation.getId())) {
                throw new BadRequestException("Khong the tra loi tin nhan tu mot cuoc tro chuyen khac.");
            }
            message.setReplyToId(requestDTO.getReplyToId());
        }
        message.setStatus(MessageStatus.SENT);
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

    @Override
    @Transactional
    public void markConversationAsRead(Integer conversationId, Integer lastMessageId) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay nguoi dung hien tai!"));

        List<Message> messagesToProcess = messageRepository.findMessagesToMarkAsRead(
                conversationId,
                currentUser.getId(),
                lastMessageId
        );

        if (messagesToProcess.isEmpty()) {
            return;
        }

        List<MessageRead> newReadRecords = new ArrayList<>();

        for (Message message : messagesToProcess) {
            // cap nhat trang thai va tao bang ghi 'da doc'
            message.setStatus(MessageStatus.SEEN);
            MessageRead readRecord = new MessageRead();
            readRecord.setMessageId(message.getId());
            readRecord.setUserId(currentUser.getId());
            readRecord.setReadAt(new Timestamp(System.currentTimeMillis()));
            newReadRecords.add(readRecord);
        }

        readRepository.saveAll(newReadRecords);
        List<Message> updatedMessages = messageRepository.saveAll(messagesToProcess);

        //gui thong bao cap nhat qua WebSocket
        String destination = "/topic/conversations/" + conversationId;
        for (Message updatedMessage : updatedMessages) {
            MessageResponseDTO messageResponseDTO = messageMapper.toMessageResponseDTO(updatedMessage);
            messagingTemplate.convertAndSend(destination, messageResponseDTO);
            log.info("Da gui cap nhat trang thai SEEN cho tin nhan ID {} den topic: {}", updatedMessage.getId(), destination);
        }
    }

    @Override
    public Page<MessageResponseDTO> searchMessagesInConversation(Integer conversationId, String keyword, Pageable pageable) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay nguoi dung hien tai!"));
        boolean isMember = conversationMemberRepository.existsByConversationIdAndUserId(conversationId, currentUser.getId());
        if (!isMember) {
            throw new AccessDeniedException("Ban khong co quyen truy cap vao cuoc tro chuyen nay!");
        }

        // tim kiem tin nhan
        Page<Message> messagePage = messageRepository.findByConversationIdAndContentContainingIgnoreCaseOrderByCreatedAtDesc(
                conversationId, keyword, pageable);
        return messagePage.map(messageMapper::toMessageResponseDTO);
    }

    @Override
    @Transactional
    public MessageResponseDTO editMessage(EditMessageRequestDTO requestDTO) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay nguoi dung hien tai!"));
        Message message = messageRepository.findById(requestDTO.getMessageId())
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay tin nhan!"));
        if (!message.getSenderId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Ban khong co quyen chinh sua tin nhan nay!");
        }
        if (message.getType() != MessageType.TEXT) {
            throw new BadRequestException("Chi co the chinh sua tin nhan van ban!");
        }

        message.setContent(requestDTO.getNewContent());
        message.setEdited(true);
        message.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        Message editedMessage = messageRepository.save(message);
        log.info("Da chinh sua tin nhan ID {} trong cuoc tro chuyen ID {}.",editedMessage.getId(),
                editedMessage.getConversationId());

        MessageResponseDTO messageResponseDTO = messageMapper.toMessageResponseDTO(message);
        String destination = "/topic/conversations/" + editedMessage.getConversationId();
        messagingTemplate.convertAndSend(destination, messageResponseDTO);
        log.info("Da day cap nhat tin nhan ID {} den WebSocket topic: {} ",editedMessage.getId(), destination);
        return messageResponseDTO;
    }


}
