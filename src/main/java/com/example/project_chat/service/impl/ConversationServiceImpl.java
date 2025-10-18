package com.example.project_chat.service.impl;

import com.example.project_chat.common.constants.ConversationType;
import com.example.project_chat.common.constants.MemberRole;
import com.example.project_chat.common.exception.BadRequestException;
import com.example.project_chat.common.exception.ResourceNotFoundException;
import com.example.project_chat.dto.group.AddMemberRequestDTO;
import com.example.project_chat.dto.group.CreateGroupRequestDTO;
import com.example.project_chat.dto.group.GroupMemberDTO;
import com.example.project_chat.dto.group.UpdateGroupRequestDTO;
import com.example.project_chat.dto.message.ConversationSummaryDTO;
import com.example.project_chat.dto.notification.UpdateNotificationSettingsDTO;
import com.example.project_chat.entity.Conversation;
import com.example.project_chat.entity.ConversationMember;
import com.example.project_chat.entity.NotificationSettings;
import com.example.project_chat.entity.User;
import com.example.project_chat.mapper.ConversationMapper;
import com.example.project_chat.repository.ConversationMemberRepository;
import com.example.project_chat.repository.ConversationRepository;
import com.example.project_chat.repository.NotificationSettingsRepository;
import com.example.project_chat.repository.UserRepository;
import com.example.project_chat.service.ConversationService;
import com.example.project_chat.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConversationServiceImpl implements ConversationService {
    private static final Logger log = LoggerFactory.getLogger(ConversationServiceImpl.class);
    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository conversationMemberRepository;
    private final UserRepository userRepository;
    private final ConversationMapper conversationMapper;
    private final FileStorageService fileStorageService;
    private final NotificationSettingsRepository notificationSettingsRepository;
    public ConversationServiceImpl(ConversationRepository conversationRepository, ConversationMemberRepository conversationMemberRepository, UserRepository userRepository, ConversationMapper conversationMapper, FileStorageService fileStorageService, NotificationSettingsRepository notificationSettingsRepository) {
        this.conversationRepository = conversationRepository;
        this.conversationMemberRepository = conversationMemberRepository;
        this.userRepository = userRepository;
        this.conversationMapper = conversationMapper;
        this.fileStorageService = fileStorageService;
        this.notificationSettingsRepository = notificationSettingsRepository;
    }

    @Override
    public Conversation findOrCreateConversation(Integer user1Id, Integer user2Id) {
        //Tim xem da co cuoc tro chuyen direct giua hai nguoi chua
        return conversationRepository.findDirectConversationIdByUserIds(user1Id,user2Id)
                .flatMap(conversationRepository::findById)
                .orElseGet(() -> createDirectConversation(user1Id,user2Id));
    }

    @Override
    public List<ConversationSummaryDTO> getConversationsForCurrentUser() {
        // lay thong tin nguoi dung hien tai
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay nguoi dung hien tai."));

        // lay danh sach cac cuoc tro chuyen ma nguoi dung tham gia
        List<Integer> conversationIds = conversationMemberRepository.findByUserId(currentUser.getId()).stream()
                .map(ConversationMember::getConversationId)
                .collect(Collectors.toList());

        List<Conversation> conversations = conversationRepository.findAllById(conversationIds);

        // chuyen doi sang dto va tra ve
        return conversations.stream()
                .map(conversation -> conversationMapper.toConversationSummaryDTO(conversation, currentUser))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ConversationSummaryDTO createGroupConversation(CreateGroupRequestDTO requestDTO) {
        //lay thong tin nguoi tao nhom
        String creatorEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User creator = userRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay nguoi dung hien tai."));
        //tao cuoc tro chuyen moi
        Conversation newGroup = new Conversation();
        newGroup.setName(requestDTO.getGroupName());
        newGroup.setType(ConversationType.GROUP);
        newGroup.setCreatedBy(creator.getId());
        //xu ly avatar
        if (requestDTO.getAvatarFile() != null && !requestDTO.getAvatarFile().isEmpty()) {
            String avatarUrl = fileStorageService.uploadFile(requestDTO.getAvatarFile());
            newGroup.setAvatarUrl(avatarUrl);
        }
        Conversation savedGroup = conversationRepository.save(newGroup);
        //them thanh vien vao nhom
        List<ConversationMember> members = new ArrayList<>();
        ConversationMember creatorMember = new ConversationMember();
        creatorMember.setConversationId(savedGroup.getId());
        creatorMember.setUserId(creator.getId());
        creatorMember.setRole(MemberRole.OWNER);
        members.add(creatorMember);
        //them thanh vien khac voi vai tro member
        for (Integer memberId : requestDTO.getMemberIds()) {
            //kiem tra xem nguoi duoc moi co chinh la nguoi tao khong
            if (!memberId.equals(creator.getId())) {
                // kiem tra xem user co ton tai khong truoc khi them vao
                userRepository.findById(memberId).ifPresent(user -> {
                    ConversationMember member = new ConversationMember();
                    member.setConversationId(savedGroup.getId());
                    member.setUserId(memberId);
                    member.setRole(MemberRole.MEMBER);
                    members.add(member);
                });
            }
        }
        conversationMemberRepository.saveAll(members);
        log.info("Da tao nhom chat '{}' voi {} thanh vien ",savedGroup.getName(),members.size());
        //map sang dto
        return conversationMapper.toConversationSummaryDTO(savedGroup,creator);
    }

    @Override
    @Transactional
    public void addMembersToGroup(Integer conversationId, AddMemberRequestDTO requestDTO)  {
        //lay thong tin nguoi dung hien taii
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay nguoi dung hien tai!"));
        //Tim cuoc tro chuyen nhom
        Conversation group = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay nhom chat"));
        if (group.getType() != ConversationType.GROUP) {
            throw new BadRequestException("Cuoc tro chuyen nay khong phai la cuoc tro chuyen nhom!");
        }
        //nguoi thuc hien them phai la nguoi tao nhom
        if (!group.getCreatedBy().equals(currentUser.getId())) {
            throw new AccessDeniedException("Chỉ người tạo nhóm mới có quyền thêm thành viên.");
        }
        //loc ra nhung nguoi chua co trong nhom
        List<Integer> newMemberIds = requestDTO.getMemberIds();

        List<ConversationMember> existingMembers = conversationMemberRepository.findByConversationIdAndUserIdIn(conversationId, newMemberIds);
        List<Integer> existingMemberIds = existingMembers.stream().map(ConversationMember::getUserId).toList();

        List<ConversationMember> newMembers = newMemberIds.stream()
                .filter(id -> !existingMemberIds.contains(id)) // loc ra nhung id chua ton tai
                .map(id -> {
                    ConversationMember member = new ConversationMember();
                    member.setConversationId(conversationId);
                    member.setUserId(id);
                    member.setRole(MemberRole.MEMBER);
                    return member;
                })
                .collect(Collectors.toList());
        if (!newMembers.isEmpty()) {
            conversationMemberRepository.saveAll(newMembers);
            log.info("Da them {} thanh vien moi vao nhom chat ID {}", newMembers.size(), conversationId);
        }
    }

    @Override
    public List<ConversationSummaryDTO> getGroupConversationsForCurrentUser() {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay nguoi dung hien tai!"));

        //lay ra danh sach nhung nhom ma nguoi dung da tham gia
        List<Conversation> groupConversations = conversationRepository.findGroupConversationsByUserId(currentUser.getId());


        return groupConversations.stream()
                .map(conversation -> conversationMapper.toConversationSummaryDTO(conversation, currentUser))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ConversationSummaryDTO updateGroupInfo(Integer groupId, UpdateGroupRequestDTO requestDTO) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay nguoi dung hien tai!"));
        Conversation group = conversationRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay nhom chat"));
        if (group.getType() != ConversationType.GROUP) {
            throw new BadRequestException("Day khong phai mot nhom chat");
        }

        ConversationMember member = conversationMemberRepository.findByConversationIdAndUserId(groupId,currentUser.getId())
                .orElseThrow(() -> new AccessDeniedException("Ban khong phai thanh vien cua nhom nay"));
        if (member.getRole() != MemberRole.OWNER && member.getRole() != MemberRole.ADMIN) {
            throw  new AccessDeniedException("Ban khong co quyen thay doi ten hoac chinh sua avatar nhom");
        }
        //cap nhat thong tin(neu co)
        boolean hasChange = false;
        if (StringUtils.hasText(requestDTO.getName())) {
            group.setName(requestDTO.getName());
            hasChange = true;
        }

        if (requestDTO.getAvatarFile() != null && !requestDTO.getAvatarFile().isEmpty()) {
            String avatarUrl = fileStorageService.uploadFile(requestDTO.getAvatarFile());
            group.setAvatarUrl(avatarUrl);
            hasChange = true;
        }
        //luu lai ket qua
        if (hasChange) {
            Conversation updatedGroup = conversationRepository.save(group);
            log.info("Da cap nhat thong tin cho nhom chat ID {}.",groupId);
            return conversationMapper.toConversationSummaryDTO(updatedGroup, currentUser);
        }
        return conversationMapper.toConversationSummaryDTO(group, currentUser);

    }

    @Override
    public List<GroupMemberDTO> getGroupMembers(Integer conversationId) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay nguoi dung hien tai"));
        if (!conversationMemberRepository.existsByConversationIdAndUserId(conversationId, currentUser.getId())) {
            throw new AccessDeniedException("Ban khong co quyen xem danh sach thanh vien cua nhom nay!");
        }

        List<Integer> memberIds = conversationMemberRepository.findByConversationId(conversationId)
                .stream()
                .map(ConversationMember::getUserId)
                .collect(Collectors.toList());

        List<User> members = userRepository.findAllById(memberIds);
        return members.stream().map(user -> new GroupMemberDTO(
                user.getId(),
                user.getDisplayName(),
                user.getAvatarUrl()
        )).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateNotificationSettings(Integer conversationId, UpdateNotificationSettingsDTO requestDTO) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay nguoi dung hien tai!"));

        if (!conversationMemberRepository.existsByConversationIdAndUserId(conversationId, currentUser.getId())) {
            throw new AccessDeniedException("Ban khong phai la thanh vien cua cuoc tro chuyen nay.");
        }

        NotificationSettings settings = notificationSettingsRepository
                .findByUserIdAndConversationId(currentUser.getId(), conversationId)
                .orElse(new NotificationSettings());

        settings.setUserId(currentUser.getId());
        settings.setConversationId(conversationId);
        settings.setEnableNotifications(requestDTO.getEnableNotifications());

        // Xử lý logic cho onlyMentions
        if (requestDTO.getOnlyMentions() != null) {
            settings.setOnlyMentions(requestDTO.getOnlyMentions());
        }

        notificationSettingsRepository.save(settings);
        log.info("Nguoi dung ID {} da cap nhat cai dat thong bao cho cuoc tro chuyen ID {}.",
                currentUser.getId(), conversationId);
    }

    private Conversation createDirectConversation(Integer user1Id, Integer user2Id) {
        //tao cuoc tro chuyen moi
        Conversation newConversation = new Conversation();
        newConversation.setType(ConversationType.DIRECT);
        Conversation savedConversation = conversationRepository.save(newConversation);

        //them 2 thanh vien vao cuoc tro chuyen
        ConversationMember member1 = new ConversationMember();
        member1.setConversationId(savedConversation.getId());
        member1.setUserId(user1Id);
        conversationMemberRepository.save(member1);

        ConversationMember member2 = new ConversationMember();
        member2.setConversationId(savedConversation.getId());
        member2.setUserId(user2Id);
        conversationMemberRepository.save(member2);

        return newConversation;
    }
}
