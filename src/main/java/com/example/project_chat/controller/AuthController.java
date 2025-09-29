package com.example.project_chat.controller;

import com.example.project_chat.dto.*;
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
        ApiResponse<?> apiResponse = new ApiResponse<>(HttpStatus.OK.value(),"Ma OTP da duoc gui den email cua ban. Vui long kiem tra thu",null);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/register/verify")
    public ResponseEntity<ApiResponse<?>> verify(@Valid @RequestBody VerifyOtpRequestDTO requestDTO) {
        authService.verifyOtp(requestDTO);
        ApiResponse<?> response = new ApiResponse<>(HttpStatus.OK.value(), "Xac thuc OTP thanh cong, vui long tao mat khau.", null);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register/password")
    public ResponseEntity<ApiResponse<?>> createPassword(@Valid @RequestBody CreatePasswordRequestDTO requestDTO) {
        authService.createAccount(requestDTO);
        ApiResponse<?> response = new ApiResponse<>(HttpStatus.OK.value(), "Tao tai khoan thanh cong!", null);
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
                "Dang xuat thanh cong!",
                null
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<?>> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO requestDTO) {
        authService.requestPasswordReset(requestDTO);
        ApiResponse<?> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "OTP de reset mat khau da duoc gui!",
                null);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-reset-otp")
    public ResponseEntity<ApiResponse<?>> verifyRequestOtp(@Valid @RequestBody VerifyOtpRequestDTO requestDTO) {
        authService.verifyOtpForPasswordReset(requestDTO);
        ApiResponse<?> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Xac thuc OTP thanh cong!",
                null);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<?>> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO requestDTO) {
        authService.resetPassword(requestDTO);
        ApiResponse<?> response = new ApiResponse<>(
          HttpStatus.OK.value(),
          "Dat lai mat khau thanh cong!",
                null
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<?>> refreshToken(@Valid @RequestBody RefreshTokenRequestDTO requestDTO) {
        RefreshTokenResponseDTO refreshTokenResponseDTO = authService.refreshToken(requestDTO);
        ApiResponse<RefreshTokenResponseDTO> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Lam moi token thanh cong!",
                refreshTokenResponseDTO
        );
        return ResponseEntity.ok(response);
    }
}
