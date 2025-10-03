package com.example.project_chat.dto.auth;

import jakarta.validation.constraints.NotBlank;

public class RefreshTokenRequestDTO {
    @NotBlank(message = "Refresh token khong duoc de trong")
    private String refreshToken;
    public String getRefreshToken() {
        return refreshToken;
    }
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
