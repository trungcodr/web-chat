package com.example.project_chat.service;

import com.example.project_chat.dto.auth.*;
import com.example.project_chat.dto.login.LoginRequestDTO;
import com.example.project_chat.dto.login.LoginResponseDTO;
import org.springframework.stereotype.Service;

@Service
public interface AuthService {
    void requestRegistration(RegisterRequestDTO registerRequestDTO);

    void verifyOtp(VerifyOtpRequestDTO verifyOtpRequestDTO);

    void createAccount(CreatePasswordRequestDTO createPasswordRequestDTO);

    LoginResponseDTO login(LoginRequestDTO loginRequestDTO);

    void logout(String email);

    void requestPasswordReset(ForgotPasswordRequestDTO forgotPasswordRequestDTO);
    void verifyOtpForPasswordReset(VerifyOtpRequestDTO verifyOtpRequestDTO);
    void resetPassword(ResetPasswordRequestDTO resetPasswordRequestDTO);

    RefreshTokenResponseDTO refreshToken(RefreshTokenRequestDTO refreshTokenRequestDTO);
}
