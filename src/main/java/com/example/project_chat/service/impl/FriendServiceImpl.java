package com.example.project_chat.service.impl;

import com.example.project_chat.common.constants.FriendStatus;
import com.example.project_chat.common.exception.BadRequestException;
import com.example.project_chat.common.exception.ResourceNotFoundException;
import com.example.project_chat.dto.friend.FriendResponseDTO;
import com.example.project_chat.dto.friend.FriendRequestDTO;
import com.example.project_chat.dto.friend.UpdateFriendRequestDTO;
import com.example.project_chat.dto.response.FriendRequestResponseDTO;
import com.example.project_chat.dto.response.SentFriendRequestResponseDTO;
import com.example.project_chat.entity.Friend;
import com.example.project_chat.entity.User;
import com.example.project_chat.mapper.FriendMapper;
import com.example.project_chat.repository.FriendRepository;
import com.example.project_chat.repository.UserRepository;
import com.example.project_chat.service.FriendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FriendServiceImpl implements FriendService {
    private static final Logger logger = LoggerFactory.getLogger(FriendServiceImpl.class);
    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private final FriendMapper friendMapper;
    public FriendServiceImpl(UserRepository userRepository, FriendRepository friendRepository, FriendMapper friendMapper) {
        this.userRepository = userRepository;
        this.friendRepository = friendRepository;
        this.friendMapper = friendMapper;
    }
    @Override
    public void sendFriendRequest(FriendRequestDTO friendRequestDTO) {
        // Lay thong tin nguoi gui yeu cau
        String requesterEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay nguoi gui yeu cau."));
        // Tim nguoi nhan yeu cau bang display name
        User receiver = userRepository.findByDisplayName(friendRequestDTO.getDisplayName())
                .orElseThrow(() -> new  ResourceNotFoundException("Không tìm thấy người nhận."));
        // nguoi dung khong the tu gui yeu cau cho chinh minh
        if(requester.getId().equals(receiver.getId())) {
            throw new BadRequestException("Khong the tu ket ban voi chinh minh!");
        }
        // Kiem tra moi quan he cua ca hai
        boolean relationshipExists = friendRepository.findByUserIdAndFriendId(requester.getId(), receiver.getId()).isPresent() ||
                friendRepository.findByUserIdAndFriendId(receiver.getId(), requester.getId()).isPresent();

        if(relationshipExists) {
            throw new BadRequestException("Yeu cau ket ban da ton tai hoac hai ban da la ban be!");
        }
        // tao va luu ban moi
        Friend newFriendRequest = new Friend();
        newFriendRequest.setUserId(requester.getId());
        newFriendRequest.setFriendId(receiver.getId());
        newFriendRequest.setStatus(FriendStatus.PENDING);
        friendRepository.save(newFriendRequest);
        logger.info("Nguoi dung ID {} da gui loi moi ket ban den nguoi dung ID {}",requester.getId(),receiver.getId());
    }

    @Override
    public List<FriendRequestResponseDTO> getReceivedFriendRequests() {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay nguoi dung hien tai"));
        List<Friend> friendRequests = friendRepository.findByFriendIdAndStatus(currentUser.getId(), FriendStatus.PENDING);
        return friendRequests.stream()
                .map(friendMapper::toFriendRequestResponseDTO)
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public void respondToFriendRequest(Integer requestId, UpdateFriendRequestDTO requestDTO) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay nguoi dung hien tai"));
        //Tim loi moi ket ban theo id
        Friend friendRequest = friendRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay loi moi ket ban"));
        //Dam bao nguoi dung hien tai la nguoi nhan dc loi moi
        if (!friendRequest.getFriendId().equals(currentUser.getId())) {
            throw new BadRequestException("Ban khong co quyen tra loi loi moi ket ban nay.");
        }
        if (friendRequest.getStatus() != FriendStatus.PENDING) {
            throw new BadRequestException("Loi moi nay da duoc tra loi truoc do");
        }
        if (requestDTO.getStatus() == FriendStatus.ACCEPTED) {
            //cap nhat trang thai loi moi
            friendRequest.setStatus(FriendStatus.ACCEPTED);
            friendRepository.save(friendRequest);
            // tao mot ban ghi hai chieu
            Friend recRelationship = new Friend();
            recRelationship.setUserId(currentUser.getId()); // nguoi chap nhan loi moi
            recRelationship.setFriendId(friendRequest.getUserId()); // nguoi gui loi moi
            recRelationship.setStatus(FriendStatus.ACCEPTED);
            friendRepository.save(recRelationship);
            logger.info("Nguoi dung ID {} da chap nhan loi moi ket ban tu nguoi dung ID {}", currentUser.getId(), friendRequest.getUserId());

        } else {
            friendRepository.delete(friendRequest);
            logger.info("Nguoi dung ID {} da tu choi loi moi ket ban tu nguoi dung ID {}", currentUser.getId(), friendRequest.getUserId());
        }

    }

    @Override
    public List<SentFriendRequestResponseDTO> getSentFriendRequests() {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay nguoi dung hien tai."));

        // tim tat ca loi moi do nguoi dung nay da gui di
        List<Friend> sentRequests = friendRepository.findByUserIdAndStatus(currentUser.getId(), FriendStatus.PENDING);

        return sentRequests.stream()
                .map(friendMapper::toSentFriendRequestResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<FriendResponseDTO> getFriendList() {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay nguoi dung hien tai!"));

        List<Friend> acceptedRelationships = friendRepository.findByUserIdAndStatus(currentUser.getId(), FriendStatus.ACCEPTED);

        return acceptedRelationships.stream()
                .map(friendMapper::toFriendResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void cancelFriendRequest(Integer requestId) {
        //Lay thong tin nguoi dung hien tai - nguoi muon huy
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay nguoi dung hien tai!"));
        // tim loi moi ket ban theo id
        Friend friendRequest = friendRepository.findById(requestId)
                .orElseThrow(() -> new BadRequestException("Ban khong co quyen thu hoi loi moi ket ban!"));
        if (friendRequest.getStatus() != FriendStatus.PENDING) {
            throw new BadRequestException("Khong the thu hoi loi moi da duoc tra loi!");
        }
        friendRepository.delete(friendRequest);
        logger.info("Nguoi dung ID {} da thu hoi loi moi ket ban ID {}", currentUser.getId(), requestId);
    }

    @Override
    @Transactional
    public void unFriend(Integer friendId) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay nguoi dung hien tai!"));
        Friend relationship1 = friendRepository.findByUserIdAndFriendId(currentUser.getId(),friendId).orElse(null);
        Friend relationship2 = friendRepository.findByUserIdAndFriendId(friendId,currentUser.getId()).orElse(null);
        if (relationship1 != null && relationship2 != null) {
            friendRepository.delete(relationship1);
            friendRepository.delete(relationship2);
            logger.info("Nguoi dung ID {} da huy ket ban voi nguoi dung ID {}", currentUser.getId(), friendId);
        } else {
            logger.warn("Khong tim moi quan he giua hai nguoi dung ID {} va nguoi dung ID {}", currentUser.getId(), friendId);
            throw new BadRequestException("Ban khong phai la ban be voi nguoi dung nay!");
        }
    }


}
