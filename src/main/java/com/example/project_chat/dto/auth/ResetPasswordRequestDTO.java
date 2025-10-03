package com.example.project_chat.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ResetPasswordRequestDTO {
    @NotBlank(message = "Email khong duoc de trong")
    @Email(message = "Email khong dung dinh dang")
    private String email;

    @NotBlank(message = "OTP khong duoc de trong")
    private String otp;

    @NotBlank(message = "Mat khau moi khong duoc de trong")
    @Size(min = 8,message = "Mat khau moi phai co 8 ky tu")
    private String newPassword;

    @NotBlank(message = "Mat khau xac nhan khong duoc de trong")
    private String newRepeatPassword;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getNewRepeatPassword() {
        return newRepeatPassword;
    }

    public void setNewRepeatPassword(String newRepeatPassword) {
        this.newRepeatPassword = newRepeatPassword;
    }
}
