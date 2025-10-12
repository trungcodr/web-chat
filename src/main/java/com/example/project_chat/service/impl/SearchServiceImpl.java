package com.example.project_chat.service.impl;

import com.example.project_chat.common.constants.FriendStatus;
import com.example.project_chat.common.exception.ResourceNotFoundException;
import com.example.project_chat.dto.friend.FriendResponseDTO;
import com.example.project_chat.dto.message.ConversationSummaryDTO;
import com.example.project_chat.dto.search.SearchResultDTO;
import com.example.project_chat.entity.Friend;
import com.example.project_chat.entity.User;
import com.example.project_chat.mapper.ConversationMapper;
import com.example.project_chat.mapper.FriendMapper;
import com.example.project_chat.repository.ConversationRepository;
import com.example.project_chat.repository.FriendRepository;
import com.example.project_chat.repository.UserRepository;
import com.example.project_chat.service.SearchService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
@Service
public class SearchServiceImpl implements SearchService {
    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private final ConversationRepository conversationRepository;
    private final FriendMapper friendMapper;
    private final ConversationMapper conversationMapper;

    public SearchServiceImpl(UserRepository userRepository, FriendRepository friendRepository, ConversationRepository conversationRepository, FriendMapper friendMapper, ConversationMapper conversationMapper) {
        this.userRepository = userRepository;
        this.friendRepository = friendRepository;
        this.conversationRepository = conversationRepository;
        this.friendMapper = friendMapper;
        this.conversationMapper = conversationMapper;
    }


    @Override
    public SearchResultDTO search(String keyword) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay nguoi dung hien tai!"));
        List<Integer> friendUserIds = friendRepository.findByUserIdAndStatus(currentUser.getId(), FriendStatus.ACCEPTED)
                .stream().map(Friend::getFriendId).collect(Collectors.toList());
        List<FriendResponseDTO> foundFriends = List.of();
        if (!friendUserIds.isEmpty()) {
            List<User> friendUsers = userRepository.findByDisplayNameContainingIgnoreCaseAndIdIn(keyword, friendUserIds);
            List<Integer> foundFriendIds = friendUsers.stream().map(User::getId).collect(Collectors.toList());
            foundFriends = friendRepository.findByUserIdAndFriendIdIn(currentUser.getId(), foundFriendIds)
                    .stream()
                    .map(friendMapper::toFriendResponseDTO)
                    .collect(Collectors.toList());
        }

        // tim kiem danh sach nhom chat
        List<ConversationSummaryDTO> foundGroups = conversationRepository
                .findGroupConversationsByUserIdAndNameContaining(currentUser.getId(), keyword)
                .stream()
                .map(conversation -> conversationMapper.toConversationSummaryDTO(conversation, currentUser))
                .collect(Collectors.toList());

        // tong hop ket qua
        SearchResultDTO result = new SearchResultDTO();
        result.setFriends(foundFriends);
        result.setGroups(foundGroups);

        return result;
    }
}
