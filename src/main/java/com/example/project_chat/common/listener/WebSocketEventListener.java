package com.example.project_chat.common.listener;

import com.example.project_chat.common.constants.FriendStatus;
import com.example.project_chat.dto.notification.UserStatusNotificationDTO;
import com.example.project_chat.repository.FriendRepository;
import com.example.project_chat.repository.UserRepository;
import com.example.project_chat.service.PresenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import java.security.Principal;


@Component
public class WebSocketEventListener {


    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private final PresenceService presenceService;
    public WebSocketEventListener(SimpMessagingTemplate messagingTemplate, UserRepository userRepository, FriendRepository friendRepository, PresenceService presenceService) {
        this.messagingTemplate = messagingTemplate;
        this.userRepository = userRepository;
        this.friendRepository = friendRepository;
        this.presenceService = presenceService;
    }


    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.wrap(event.getMessage());
        Principal principal = headerAccessor.getUser();

        if (principal != null) {
            String username = principal.getName();
            logger.info("User Disconnected: {}", username);

            userRepository.findByEmail(username).ifPresent(user -> {
                presenceService.userOffline(user.getId()); // Cập nhật trạng thái offline

                // Tạo thông báo offline
                var notification = new UserStatusNotificationDTO(user.getId(), false);

                // Lấy danh sách bạn bè và thông báo cho họ
                friendRepository.findByUserIdAndStatus(user.getId(), FriendStatus.ACCEPTED)
                        .forEach(friendship -> {
                            String destination = "/topic/status/" + friendship.getFriendId();
                            messagingTemplate.convertAndSend(destination, notification);
                        });
            });
        }
    }


}
