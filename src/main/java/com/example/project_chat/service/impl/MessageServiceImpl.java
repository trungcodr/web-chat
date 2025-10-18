package com.example.project_chat.service.impl;

import com.example.project_chat.common.constants.MemberRole;
import com.example.project_chat.common.constants.MessageStatus;
import com.example.project_chat.common.constants.MessageType;
import com.example.project_chat.common.exception.BadRequestException;
import com.example.project_chat.common.exception.ResourceNotFoundException;
import com.example.project_chat.dto.message.EditMessageRequestDTO;
import com.example.project_chat.dto.message.ForwardMessageRequestDTO;
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
import java.util.Optional;
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
    private final UserConversationClearRepository userConversationClearRepository;
    private final PinnedMessageRepository pinnedMessageRepository;
    public MessageServiceImpl(MessageRepository messageRepository, MessageReadRepository readRepository, UserRepository userRepository, ConversationRepository conversationRepository, ConversationService conversationService, FileStorageService fileStorageService, MessageMapper messageMapper, ConversationMemberRepository conversationMemberRepository, SimpMessagingTemplate messagingTemplate, UserConversationClearRepository userConversationClearRepository, PinnedMessageRepository pinnedMessageRepository) {
        this.messageRepository = messageRepository;
        this.readRepository = readRepository;
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
        this.conversationService = conversationService;
        this.fileStorageService = fileStorageService;
        this.messageMapper = messageMapper;
        this.conversationMemberRepository = conversationMemberRepository;
        this.messagingTemplate = messagingTemplate;
        this.userConversationClearRepository = userConversationClearRepository;
        this.pinnedMessageRepository = pinnedMessageRepository;
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
            case VOICE:
                MultipartFile file1 = requestDTO.getFile();
                if (file1 == null || file1.isEmpty()) {
                    throw new BadRequestException("File khong duoc de trong!.");
                }
                String fileUrl1 = fileStorageService.uploadFile(file1);
                message.setFileUrl(fileUrl1);
                message.setFileName(file1.getOriginalFilename());
                message.setFileSize((int) file1.getSize());
                break;
            case LOCATION:
                if (requestDTO.getLatitude() == null || requestDTO.getLongitude() == null) {
                    throw new BadRequestException("Toa do khong hop le!");
                }
                message.setLatitude(requestDTO.getLatitude());
                message.setLongitude(requestDTO.getLongitude());
                break;
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
        // kiem tra xem nguoi dung da xoa cuoc tro chuyen nay truoc do hay chua
        Optional<UserConversationClear> clearRecord = userConversationClearRepository
                .findByUserIdAndConversationId(currentUser.getId(), conversationId);

        Page<Message> messagePage;
        if (clearRecord.isPresent()) {
            //neu co, chi lay nhung tin nhan duoc tao sau thoi diem nguoi dung da xoa
            messagePage = messageRepository.findByConversationIdAndCreatedAtAfterOrderByCreatedAtDesc(
                    conversationId,
                    clearRecord.get().getClearedAt(),
                    pageable
            );
        } else {
            // neu khong, lay toan bo lich su  tin nhan (da duoc phan trang)
            messagePage = messageRepository.findByConversationIdOrderByCreatedAtDesc(conversationId, pageable);
        }

        // map tin nhan tu entity sang dto
        Page<MessageResponseDTO> messageDtoPage = messagePage.map(messageMapper::toMessageResponseDTO);

        // lay danh sach thanh vien cua cuoc tro chuyen
        List<ConversationMember> members = conversationMemberRepository.findByConversationId(conversationId);
        List<Integer> memberIds = members.stream().map(ConversationMember::getUserId).collect(Collectors.toList());
        List<User> memberUsers = userRepository.findAllById(memberIds);
        List<UserResponseDTO> memberDtos = memberUsers.stream()
                .map(UserMapper::toUserResponseDTO)
                .collect(Collectors.toList());

        // tao doi tuong tra ve chua ca tin nhan va danh sach thanh vien
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

    @Override
    @Transactional
    public void clearHistoryForCurrentUser(Integer conversationId) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay nguoi dung hien tai!"));

        if (!conversationMemberRepository.existsByConversationIdAndUserId(conversationId, currentUser.getId())) {
            throw new AccessDeniedException("Ban khong phai la thanh vien cuoc tro chuyen!");
        }

        UserConversationClear clearRecord = userConversationClearRepository
                .findByUserIdAndConversationId(currentUser.getId(),conversationId)
                .orElse(new UserConversationClear());

        clearRecord.setUserId(currentUser.getId());
        clearRecord.setConversationId(conversationId);
        clearRecord.setClearedAt(new Timestamp(System.currentTimeMillis()));

        userConversationClearRepository.save(clearRecord);
        log.info("Nguoi dung ID {} da xoa lich su phia minh cho cuoc tro chuyen ID {}.",currentUser.getId(),conversationId);

    }

    @Override
    public void forwardMessage(ForwardMessageRequestDTO requestDTO) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay nguoi dung hien tai!"));

        Message originalMessage = messageRepository.findById(requestDTO.getMessageId())
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay tin nhan goc!"));

        if (!conversationMemberRepository.existsByConversationIdAndUserId(originalMessage.getConversationId(), currentUser.getId())) {
            throw new AccessDeniedException("Ban khong co quyen truy cap vao cuooc tro chuyen nay!");
        }

        List<Message> forwardedMessages = new ArrayList<>();

        for (Integer targetConversationId : requestDTO.getConversationIds()) {
            if (!conversationMemberRepository.existsByConversationIdAndUserId(targetConversationId, currentUser.getId())) {
                log.warn("Nguoi dung {} khong phai la thanh vien cuoc tro chuyen {}.", currentUser.getId(), targetConversationId);
                continue;
            }

            Message forwardedMessage = new Message();

            //sao chep noi dung tu tin nhan goc
            forwardedMessage.setType(originalMessage.getType());
            forwardedMessage.setContent(originalMessage.getContent());
            forwardedMessage.setFileUrl(originalMessage.getFileUrl());
            forwardedMessage.setFileName(originalMessage.getFileName());
            forwardedMessage.setFileSize(originalMessage.getFileSize());
            forwardedMessage.setStickerId(originalMessage.getStickerId());
            forwardedMessage.setLatitude(originalMessage.getLatitude());
            forwardedMessage.setLongitude(originalMessage.getLongitude());

            //thiet lap thong tin moi cho tin nhan chuyen tiep
            forwardedMessage.setSenderId(currentUser.getId());
            forwardedMessage.setConversationId(targetConversationId);
            forwardedMessage.setStatus(MessageStatus.SENT);
            forwardedMessage.setCreatedAt(new Timestamp(System.currentTimeMillis()));

            forwardedMessages.add(forwardedMessage);
        }

        // luu tin nhan vao csdl
        if (!forwardedMessages.isEmpty()) {
            List<Message> savedMessages = messageRepository.saveAll(forwardedMessages);
            log.info("Nguoi dung ID {} da chuyen tiep tin nhan ID {} den {} cuoc tro chuyen.", currentUser.getId(), originalMessage.getId(), savedMessages.size());

            // gui thong bao websocket den tung cuoc tro chuyen
            savedMessages.forEach(msg -> {
                MessageResponseDTO messageResponseDTO = messageMapper.toMessageResponseDTO(msg);
                String destination = "/topic/conversations/" + msg.getConversationId();
                messagingTemplate.convertAndSend(destination, messageResponseDTO);
            });
        }
    }

    @Override
    public void pinMessage(Integer messageId) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay nguoi dung hien tai!"));

        Message messagePin = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay tin nhan de ghim!"));
        Integer conversationId = messagePin.getConversationId();

        if (!conversationMemberRepository.existsByConversationIdAndUserId(conversationId, currentUser.getId())) {
            throw new AccessDeniedException("Ban khong co quyen ghim tin nhan trong cuoc tro chuyen nay!");
        }

        //kiem tra tin nhan da duoc ghim chua
        if (pinnedMessageRepository.existsByConversationIdAndMessageId(conversationId, messageId)) {
            throw new BadRequestException("Tin nhan nay da duoc ghim");
        }

        PinnedMessage pinnedMessage = new PinnedMessage();
        pinnedMessage.setConversationId(conversationId);
        pinnedMessage.setMessageId(messageId);
        pinnedMessage.setPinnedBy(currentUser.getId());
        pinnedMessage.setPinnedAt(new Timestamp(System.currentTimeMillis()));
        pinnedMessageRepository.save(pinnedMessage);
        log.info("Nguoi dung ID {} da ghim tin nhan ID{} trong cuoc tro chuyen ID{}.",currentUser.getId(),messageId,conversationId);
        //gui thong bao ws
        String destination = "/topic/conversations/" + conversationId + "/pin";
        MessageResponseDTO messageResponseDTO = messageMapper.toMessageResponseDTO(messagePin);
        messagingTemplate.convertAndSend(destination, messageResponseDTO);
    }

    @Override
    public List<MessageResponseDTO> getPinnedMessages(Integer conversationId) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay nguoi dung hien tai!"));
        if (!conversationMemberRepository.existsByConversationIdAndUserId(conversationId, currentUser.getId())) {
            throw new AccessDeniedException("Ban khong co quyen xem tin nhan duoc ghim cua cuoc tro chuyen nay!");
        }
        //lay danh sach tin nhan da ghim
        List<PinnedMessage> pinnedMessages = pinnedMessageRepository.findByConversationIdOrderByPinnedAtDesc(conversationId);

        List<Integer> messageIds = pinnedMessages.stream()
                .map(PinnedMessage::getMessageId)
                .collect(Collectors.toList());

        return messageRepository.findAllById(messageIds).stream()
                .map(messageMapper::toMessageResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void unpinMessage(Integer messageId) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay nguoi dung hien tai!"));

        Message messageToUnpin = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay tin nhan de ghim!"));

        Integer conversationId = messageToUnpin.getConversationId();

        // kiem tra nguoi dung co phai thanh vien cuoc hoi thoai
        ConversationMember member = conversationMemberRepository.findByConversationIdAndUserId(conversationId, currentUser.getId())
                .orElseThrow(() -> new AccessDeniedException("ban khong co quyen bo ghim tin nhan trong cuoo hoi thoai nay"));

        PinnedMessage pinnedMessage = pinnedMessageRepository.findByConversationIdAndMessageId(conversationId, messageId)
                .orElseThrow(() -> new BadRequestException("Tin nhan nay chua duoc ghim!"));

        // chi nguoi da ghim owner.
        boolean isOwner = member.getRole() == MemberRole.OWNER;
        boolean isAdmin = member.getRole() == MemberRole.ADMIN;
        boolean isPinned = pinnedMessage.getPinnedBy().equals(currentUser.getId());

        if (!isOwner && !isAdmin && !isPinned) {
            throw new AccessDeniedException("Chỉ người ghim, quản trị viên hoặc chủ nhóm mới có quyền bỏ ghim.");
        }

        // xoab ban ghi ghim
        pinnedMessageRepository.delete(pinnedMessage);
        log.info("Nguoi dung ID {} da bo ghim tin nhan ID {} trong cuoc tro chuyen ID {}.", currentUser.getId(), messageId, conversationId);

        // gui thong tin ws
        String destination = "/topic/conversations/" + conversationId + "/unpin";
        //gui di messageId de giao dien biet tin nhan nao da duoc bo ghim
        messagingTemplate.convertAndSend(destination, java.util.Collections.singletonMap("messageId", messageId));
    }

}
