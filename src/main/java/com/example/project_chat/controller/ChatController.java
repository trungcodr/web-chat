package com.example.project_chat.controller;

import com.example.project_chat.common.constants.FriendStatus;
import com.example.project_chat.dto.message.TypingUserDTO;
import com.example.project_chat.dto.notification.UserStatusNotificationDTO;
import com.example.project_chat.entity.Friend;
import com.example.project_chat.repository.FriendRepository;
import com.example.project_chat.repository.UserRepository;
import com.example.project_chat.service.PresenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller

public class ChatController {
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private final PresenceService presenceService;
    public ChatController(SimpMessagingTemplate messagingTemplate, UserRepository userRepository, FriendRepository friendRepository, PresenceService presenceService) {
        this.messagingTemplate = messagingTemplate;

        this.userRepository = userRepository;
        this.friendRepository = friendRepository;
        this.presenceService = presenceService;
    }


    @MessageMapping("/chat.typing")
    public void handleTyping(@Payload TypingUserDTO typingUserDTO) {
        String destination = "/topic/conversations/" + typingUserDTO.getConversationId() + "/typing";
        messagingTemplate.convertAndSend(destination, typingUserDTO);
    }

    @MessageMapping("/chat.online")
    public void userIsOnline(Authentication authentication) {
        if (authentication == null) {
            return;
        }
        String username = authentication.getName();
        userRepository.findByEmail(username).ifPresent(user -> {
            Integer currentUserId = user.getId();

            // Nếu đã online rồi thì không xử lý nữa
            if (presenceService.isUserOnline(currentUserId)) {
                return;
            }

            logger.info("Processing online status for user: {}, ID: {}", username, currentUserId);
            presenceService.userOnline(currentUserId);

            List<Friend> friends = friendRepository.findByUserIdAndStatus(currentUserId, FriendStatus.ACCEPTED);
            logger.info("User ID {} has {} accepted friends.", currentUserId, friends.size());

            // Thông báo cho tất cả bạn bè rằng TÔI đang online.
            var notificationForFriends = new UserStatusNotificationDTO(currentUserId, true);
            friends.forEach(friendship -> {
                Integer friendId = friendship.getFriendId();
                String friendDestination = "/topic/status/" + friendId;
                messagingTemplate.convertAndSend(friendDestination, notificationForFriends);
                logger.info("==> Sent online status OF user {} TO friend {}", currentUserId, friendId);
            });

            // Thông báo cho TÔI về tất cả bạn bè đang online.
            String selfDestination = "/topic/status/" + currentUserId;
            friends.forEach(friendship -> {
                Integer friendId = friendship.getFriendId();
                if (presenceService.isUserOnline(friendId)) {
                    var notificationForSelf = new UserStatusNotificationDTO(friendId, true);
                    messagingTemplate.convertAndSend(selfDestination, notificationForSelf);
                    logger.info("==> Sent status of ONLINE friend {} BACK to user {}", friendId, currentUserId);
                }
            });
        });
    }

}
