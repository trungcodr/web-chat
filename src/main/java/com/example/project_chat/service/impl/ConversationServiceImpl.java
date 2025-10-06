package com.example.project_chat.service.impl;

import com.example.project_chat.common.constants.ConversationType;
import com.example.project_chat.common.exception.ResourceNotFoundException;
import com.example.project_chat.dto.message.ConversationSummaryDTO;
import com.example.project_chat.entity.Conversation;
import com.example.project_chat.entity.ConversationMember;
import com.example.project_chat.entity.User;
import com.example.project_chat.mapper.ConversationMapper;
import com.example.project_chat.repository.ConversationMemberRepository;
import com.example.project_chat.repository.ConversationRepository;
import com.example.project_chat.repository.UserRepository;
import com.example.project_chat.service.ConversationService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConversationServiceImpl implements ConversationService {
    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository conversationMemberRepository;
    private final UserRepository userRepository;
    private final ConversationMapper conversationMapper;

    public ConversationServiceImpl(ConversationRepository conversationRepository, ConversationMemberRepository conversationMemberRepository, UserRepository userRepository, ConversationMapper conversationMapper) {
        this.conversationRepository = conversationRepository;
        this.conversationMemberRepository = conversationMemberRepository;
        this.userRepository = userRepository;
        this.conversationMapper = conversationMapper;
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
