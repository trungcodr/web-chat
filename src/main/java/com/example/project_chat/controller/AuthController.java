package com.example.project_chat.controller;

import com.example.project_chat.dto.auth.*;
import com.example.project_chat.dto.login.LoginRequestDTO;
import com.example.project_chat.dto.login.LoginResponseDTO;
import com.example.project_chat.dto.response.ApiResponse;
import com.example.project_chat.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> register(@Valid @RequestBody RegisterRequestDTO registerRequestDTO) {
        authService.requestRegistration(registerRequestDTO);
        ApiResponse<?> apiResponse = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Mã OTP đã được gửi đến email của bạn. Vui lòng kiểm tra thử",
                null);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/register/verify")
    public ResponseEntity<ApiResponse<?>> verify(@Valid @RequestBody VerifyOtpRequestDTO requestDTO) {
        authService.verifyOtp(requestDTO);
        ApiResponse<?> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Xác thực OTP thành công, vui lòng tạo mật khẩu.", null);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register/password")
    public ResponseEntity<ApiResponse<?>> createPassword(@Valid @RequestBody CreatePasswordRequestDTO requestDTO) {
        authService.createAccount(requestDTO);
        ApiResponse<?> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Tạo tài khoản thành công!", null);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        LoginResponseDTO loginResponse = authService.login(loginRequest);
        ApiResponse<LoginResponseDTO> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Đăng nhập thành công!",
                loginResponse
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout() {
        //Lay thong tin xac thuc cua nguoi dung dang dang nhap
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        //Goi service de xu ly dang xuat
        authService.logout(email);
        ApiResponse<?> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Đăng xuất thành công!",
                null
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<?>> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO requestDTO) {
        authService.requestPasswordReset(requestDTO);
        ApiResponse<?> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "OTP để reset mật khẩu đã được gửi!",
                null);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-reset-otp")
    public ResponseEntity<ApiResponse<?>> verifyRequestOtp(@Valid @RequestBody VerifyOtpRequestDTO requestDTO) {
        authService.verifyOtpForPasswordReset(requestDTO);
        ApiResponse<?> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Xác thực OTP thành công!",
                null);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<?>> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO requestDTO) {
        authService.resetPassword(requestDTO);
        ApiResponse<?> response = new ApiResponse<>(
          HttpStatus.OK.value(),
          "Đặt lại mật khẩu thành công!",
                null
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<?>> refreshToken(@Valid @RequestBody RefreshTokenRequestDTO requestDTO) {
        RefreshTokenResponseDTO refreshTokenResponseDTO = authService.refreshToken(requestDTO);
        ApiResponse<RefreshTokenResponseDTO> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Làm mới token thành công!",
                refreshTokenResponseDTO
        );
        return ResponseEntity.ok(response);
    }
}
