package com.example.project_chat.service.impl;

import com.example.project_chat.common.exception.BadRequestException;
import com.example.project_chat.dto.auth.*;
import com.example.project_chat.dto.login.LoginRequestDTO;
import com.example.project_chat.dto.login.LoginResponseDTO;
import com.example.project_chat.entity.Role;
import com.example.project_chat.entity.User;
import com.example.project_chat.entity.UserRole;
import com.example.project_chat.repository.RoleRepository;
import com.example.project_chat.repository.UserRepository;
import com.example.project_chat.repository.UserRoleRepository;
import com.example.project_chat.security.CustomUserDetailsService;
import com.example.project_chat.security.JwtTokenProvider;
import com.example.project_chat.service.AuthService;
import com.example.project_chat.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String,Object> redisTemplate;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    public AuthServiceImpl(UserRepository userRepository, RoleRepository roleRepository, UserRoleRepository userRoleRepository, PasswordEncoder passwordEncoder, RedisTemplate<String, Object> redisTemplate, EmailService emailService, AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider, CustomUserDetailsService customUserDetailsService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.redisTemplate = redisTemplate;
        this.emailService = emailService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Value("${jwt.refresh-token-expiration-ms}")
    private Long refreshTokenExpirationMs;
    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
    private static final String OTP_PREFIX = "otp:";
    private static final String REFRESH_TOKEN_PREFIX = "refreshToken:";
    private static final String RESET_OTP_PREFIX = "otp:reset:";

    @Override
    public void requestRegistration(RegisterRequestDTO registerRequestDTO) {
        System.out.println("1. Bắt đầu xử lý đăng ký cho email: " + registerRequestDTO.getEmail());
        if (userRepository.existsByEmail(registerRequestDTO.getEmail())) {
            System.out.println("LỖI: Email đã tồn tại.");
            throw new BadRequestException("Email" + registerRequestDTO.getEmail() + "da duoc dang ky!");
        }

        String otp = generateOtp();
        String redisKey = OTP_PREFIX + registerRequestDTO.getEmail();
        System.out.println("3. Đã tạo OTP: " + otp + " với key: " + redisKey);

        try {
            redisTemplate.opsForValue().set(redisKey, otp, 5, TimeUnit.MINUTES);
            System.out.println("4. ĐÃ LƯU OTP VÀO REDIS THÀNH CÔNG.");
        } catch (Exception e) {
            System.out.println("LỖI NGHIÊM TRỌNG: KHÔNG THỂ LƯU OTP VÀO REDIS.");
            e.printStackTrace();
            throw e;
        }

        System.out.println("5. Bắt đầu gửi email...");
        emailService.sendOtpEmail(registerRequestDTO.getEmail(), otp);
        System.out.println("6. Gửi email thành công. Hoàn tất!");
    }

    @Override
    public void verifyOtp(VerifyOtpRequestDTO verifyOtpRequestDTO) {
        String redisKey = OTP_PREFIX + verifyOtpRequestDTO.getEmail();
        String cachedOtp = (String) redisTemplate.opsForValue().get(redisKey);

        if (cachedOtp == null || !cachedOtp.equals(verifyOtpRequestDTO.getOtp())) {
            throw new BadRequestException("OTP khong hop le hoac da het han!");
        }
    }


    @Override
    @Transactional
    public void createAccount(CreatePasswordRequestDTO createPasswordRequestDTO) {
        verifyOtp(new VerifyOtpRequestDTO(){{
            setEmail(createPasswordRequestDTO.getEmail());
            setOtp(createPasswordRequestDTO.getOtp());
        }});

        if (!createPasswordRequestDTO.getPassword().equals(createPasswordRequestDTO.getRepeatPassword())) {
            throw new BadRequestException("Mat khau xac nhan khong khop!");
        }

        //Tao va luu user moi
        User newUser = new User();
        newUser.setEmail(createPasswordRequestDTO.getEmail());
        newUser.setPassword(passwordEncoder.encode(createPasswordRequestDTO.getPassword()));
        User savedUser = userRepository.save(newUser);

        //Gan vai tro USER mac dinh
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vai trò 'USER'. Vui lòng kiểm tra dữ liệu khởi tạo."));
        UserRole newUserRole = new UserRole();
        newUserRole.setUserId(savedUser.getId());
        newUserRole.setRoleId(userRole.getId());
        userRoleRepository.save(newUserRole);

        //Xoa otp khoi redis sau khi hoan tat
        String redisKey = OTP_PREFIX + createPasswordRequestDTO.getEmail();
        redisTemplate.delete(redisKey);
    }

    @Override
    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) {
        //Xac thuc nguoi dung ban gemail va mat khau
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDTO.getEmail(),
                        loginRequestDTO.getPassword()
                )
        );

        //Xac thuc thanh cong dat thng tin vao SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String username = authentication.getName();
        logger.info("Nguoi dung {} da xac thuc thanh cong.", username);
        //Tao access token
        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        //Tao refresh token
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        try {
            String redisKey = REFRESH_TOKEN_PREFIX + username;
            redisTemplate.opsForValue().set(
                    redisKey,
                    refreshToken,
                    refreshTokenExpirationMs,
                    TimeUnit.MILLISECONDS
            );
            logger.info("Da luu refresh token vao redis cho {}.", username);
        } catch (Exception e) {
            logger.error("Khong the luu refresh token vao redis cho {}: {}", username, e.getMessage());
        }
        return new LoginResponseDTO(accessToken, refreshToken);
    }

    @Override
    public void logout(String email) {
        String redisKey = REFRESH_TOKEN_PREFIX + email;
        try{
            //Xoa key khoi redis
            redisTemplate.delete(redisKey);
            logger.info("Da dang xuat va xoa refresh token cho {}.", email);
        } catch (Exception e) {
            logger.error("Loi kho xoa refresh token khoi redis cho {}: {}", email, e.getMessage());
        }
    }

    @Override
    public void requestPasswordReset(ForgotPasswordRequestDTO forgotPasswordRequestDTO) {
        User user = userRepository.findByEmail(forgotPasswordRequestDTO.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay nguoi dung voi email: " + forgotPasswordRequestDTO.getEmail()));

        // Tạo OTP, lưu vào Redis với prefix khác
        String otp = generateOtp();
        String redisKey = RESET_OTP_PREFIX + forgotPasswordRequestDTO.getEmail();
        redisTemplate.opsForValue().set(redisKey, otp, 5, TimeUnit.MINUTES);

        // Gửi email chứa OTP
        emailService.sendOtpEmail(forgotPasswordRequestDTO.getEmail(), otp);
        logger.info("Da gui OTP reset mat khau cho {}.", forgotPasswordRequestDTO.getEmail());
    }

    @Override
    public void verifyOtpForPasswordReset(VerifyOtpRequestDTO verifyOtpRequestDTO) {
        String redisKey = RESET_OTP_PREFIX + verifyOtpRequestDTO.getEmail();
        String cachedOtp = (String) redisTemplate.opsForValue().get(redisKey);

        if (cachedOtp == null || !cachedOtp.equals(verifyOtpRequestDTO.getOtp())) {
            throw new BadRequestException("OTP không hợp lệ hoặc đã hết hạn!");
        }
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequestDTO resetPasswordRequestDTO) {
        String redisKey = RESET_OTP_PREFIX + resetPasswordRequestDTO.getEmail();
        String cachedOtp = (String) redisTemplate.opsForValue().get(redisKey);
        if (cachedOtp == null || !cachedOtp.equals(resetPasswordRequestDTO.getOtp())) {
            throw new BadRequestException("OTP khong hop le hoac da het han!");
        }

        if (!resetPasswordRequestDTO.getNewPassword().equals(resetPasswordRequestDTO.getNewRepeatPassword())) {
            throw new BadRequestException("Mật khau xac nhan khong khop!");
        }

        User user = userRepository.findByEmail(resetPasswordRequestDTO.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay nguoi dung voi email: " + resetPasswordRequestDTO.getEmail()));

        user.setPassword(passwordEncoder.encode(resetPasswordRequestDTO.getNewPassword()));
        userRepository.save(user);

        redisTemplate.delete(redisKey);
        logger.info("Nguoi dung {} da dat lai mat khau thanh cong.", resetPasswordRequestDTO.getEmail());

    }

    @Override
    public RefreshTokenResponseDTO refreshToken(RefreshTokenRequestDTO refreshTokenRequestDTO) {
        String requestRefreshToken = refreshTokenRequestDTO.getRefreshToken();
        if (!jwtTokenProvider.validateToken(requestRefreshToken)) {
            throw new BadRequestException("Refresh token khong hop le hoac da het han!");
        }
        String email = jwtTokenProvider.getUsernameFromToken(requestRefreshToken);
        String redisKey = REFRESH_TOKEN_PREFIX + email;
        String tokenFromRedis = (String) redisTemplate.opsForValue().get(redisKey);
        if (tokenFromRedis == null || !tokenFromRedis.equals(requestRefreshToken)) {
            throw new BadRequestException("Refresh token khong ton tai trong he thong hoac da bi thu hoi!");
        }
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        String newAccessToken = jwtTokenProvider.generateAccessToken(authentication);
        logger.info("Da lam moi access token thanh cong cho {}.", email);
        return new RefreshTokenResponseDTO(newAccessToken);
    }

    private String generateOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }
}
