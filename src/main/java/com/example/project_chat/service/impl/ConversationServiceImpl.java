package com.example.project_chat.service.impl;

import com.example.project_chat.common.constants.ConversationType;
import com.example.project_chat.entity.Conversation;
import com.example.project_chat.entity.ConversationMember;
import com.example.project_chat.repository.ConversationMemberRepository;
import com.example.project_chat.repository.ConversationRepository;
import com.example.project_chat.service.ConversationService;
import org.springframework.stereotype.Service;

@Service
public class ConversationServiceImpl implements ConversationService {
    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository conversationMemberRepository;

    public ConversationServiceImpl(ConversationRepository conversationRepository, ConversationMemberRepository conversationMemberRepository) {
        this.conversationRepository = conversationRepository;
        this.conversationMemberRepository = conversationMemberRepository;
    }

    @Override
    public Conversation findOrCreateConversation(Integer user1Id, Integer user2Id) {
        //Tim xem da co cuoc tro chuyen direct giua hai nguoi chua
        return conversationRepository.findDirectConversationIdByUserIds(user1Id,user2Id)
                .flatMap(conversationRepository::findById)
                .orElseGet(() -> createDirectConversation(user1Id,user2Id));
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
