package com.example.project_chat.security;

import com.example.project_chat.entity.User;
import com.example.project_chat.repository.ConversationMemberRepository;
import com.example.project_chat.repository.UserRepository;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Principal;

@Component
public class JwtChannelInterceptor implements ChannelInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(JwtChannelInterceptor.class);
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final ConversationMemberRepository conversationMemberRepository;
    private final UserRepository userRepository;
    public JwtChannelInterceptor(JwtTokenProvider jwtTokenProvider, CustomUserDetailsService customUserDetailsService, ConversationMemberRepository conversationMemberRepository, UserRepository userRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.customUserDetailsService = customUserDetailsService;
        this.conversationMemberRepository = conversationMemberRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Message<?> preSend(@NotNull Message<?> message, @NotNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // Chỉ xử lý khi client thực hiện kết nối (CONNECT)
        if (accessor == null || accessor.getCommand() == null) {
            return message; // Bỏ qua nếu không phải là tin nhắn STOMP hợp lệ
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            // Lấy token từ header "Authorization"
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
                String jwt = authHeader.substring(7);
                if (jwtTokenProvider.validateToken(jwt)) {
                    String username = jwtTokenProvider.getUsernameFromToken(jwt);
                    UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

                    // Tạo đối tượng xác thực và đặt vào SecurityContext
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    // Gán principal cho session WebSocket
                    accessor.setUser(authentication);
                    logger.info("Authenticated user {} for WebSocket session.", username);
                }
            }
        }

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            Principal principal = accessor.getUser();
            String destination = accessor.getDestination();

            if (principal != null && destination != null && destination.startsWith("/topic/conversations/")) {
                try {
                    String[] parts = destination.split("/");
                    Integer conversationId = Integer.parseInt(parts[3]);

                    // Dùng email từ Principal để tìm User và lấy ID
                    String username = principal.getName();
                    User user = userRepository.findByEmail(username)
                            .orElseThrow(() -> new AccessDeniedException("Không tìm thấy người dùng.")); // Lấy người dùng từ DB

                    if (!conversationMemberRepository.existsByConversationIdAndUserId(conversationId, user.getId())) {
                        throw new AccessDeniedException("Bạn không có quyền truy cập vào cuộc trò chuyện này.");
                    }
                } catch (NumberFormatException e) {
                    logger.warn("Invalid conversationId in WebSocket destination: {}", destination);
                    throw new AccessDeniedException("Destination không hợp lệ.");
                }
            }
        }

        return message;
    }
}