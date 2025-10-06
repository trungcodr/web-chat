package com.example.project_chat.mapper;

import com.example.project_chat.common.constants.ConversationType;
import com.example.project_chat.dto.message.ConversationSummaryDTO;
import com.example.project_chat.dto.message.LastMessageDTO;
import com.example.project_chat.entity.Conversation;
import com.example.project_chat.entity.ConversationMember;
import com.example.project_chat.entity.Message;
import com.example.project_chat.entity.User;
import com.example.project_chat.repository.ConversationMemberRepository;
import com.example.project_chat.repository.MessageRepository;
import com.example.project_chat.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ConversationMapper {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ConversationMemberRepository conversationMemberRepository;

    public ConversationMapper(MessageRepository messageRepository, UserRepository userRepository, ConversationMemberRepository conversationMemberRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.conversationMemberRepository = conversationMemberRepository;
    }

    public ConversationSummaryDTO toConversationSummaryDTO(Conversation conversation, User currentUser) {
        ConversationSummaryDTO dto = new ConversationSummaryDTO();
        dto.setId(conversation.getId());

        // 1. lay va map tin nhan cuoi cung
        Optional<Message> lastMessageOpt = messageRepository.findFirstByConversationIdOrderByCreatedAtDesc(conversation.getId());
        if (lastMessageOpt.isPresent()) {
            Message lastMessage = lastMessageOpt.get();
            LastMessageDTO lastMessageDTO = new LastMessageDTO();
            lastMessageDTO.setSenderId(lastMessage.getSenderId());
            boolean isSenderYou = lastMessage.getSenderId().equals(currentUser.getId());
            lastMessageDTO.setYou(isSenderYou);
            lastMessageDTO.setType(lastMessage.getType());
            lastMessageDTO.setCreatedAt(lastMessage.getCreatedAt());
            // hien thi tom tat noi dung cho cac loai tin khac nhau
            String contentPreview;
            switch (lastMessage.getType()) {
                case IMAGE:
                    contentPreview = "Da gui mot anh";
                    break;
                case FILE:
                    contentPreview = "Da gui mot tep";
                    break;
                case STICKER:
                default:
                    contentPreview = lastMessage.getContent();
                    break;
            }
            lastMessageDTO.setContent(contentPreview);
            dto.setLastMessage(lastMessageDTO);
        }
        //lay danh sach thanh vien va map sang danh sach id
        List<ConversationMember> members = conversationMemberRepository.findByConversationId(conversation.getId());
        List<Integer> memberIds = members.stream()
                .map(ConversationMember::getUserId)
                .collect(Collectors.toList());
        dto.setMembers(memberIds);
        // lay ten avatar cua cuoc tro chuyen
        if (conversation.getType() == ConversationType.DIRECT) {
            // voi chat 1-1, tim nguoi con lai va lay thong tin cua ho
            Optional<Integer> otherUserIdOpt = members.stream()
                    .map(ConversationMember::getUserId)
                    .filter(userId -> !userId.equals(currentUser.getId()))
                    .findFirst();

            if (otherUserIdOpt.isPresent()) {
                userRepository.findById(otherUserIdOpt.get()).ifPresent(otherUser -> {
                    dto.setName(otherUser.getDisplayName());
                    dto.setAvatarUrl(otherUser.getAvatarUrl());
                });
            }
        } else {
            dto.setName(conversation.getName());
            dto.setAvatarUrl(conversation.getAvatarUrl());
        }

        return dto;
    }
}