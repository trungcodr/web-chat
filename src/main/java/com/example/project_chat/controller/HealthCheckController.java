package com.example.project_chat.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {
    // Endpoint để xử lý yêu cầu GET tới đường dẫn gốc (/)
    @GetMapping("/")
    public String getRootStatus() {
        // Trả về một phản hồi đơn giản với mã HTTP 200 OK
        return "Project Chat API is running smoothly. Ready for frontend connection.";
    }
}
