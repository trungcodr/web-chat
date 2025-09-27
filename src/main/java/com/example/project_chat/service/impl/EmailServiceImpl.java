package com.example.project_chat.service.impl;

import com.example.project_chat.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);
    private final JavaMailSender mailSender;

    //Lay email nguoi gui tu file application.yml
    @Value("${spring.mail.username")
    private String fromEmail;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendOtpEmail(String to, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Ma xac thuc OTP cua ban");
            message.setText("Ma OTP de dang ky tai khoan cua ban la: " + otp);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Loi khi gui email den OTP den {}: {}", to, e.getMessage());
        }
    }
}
