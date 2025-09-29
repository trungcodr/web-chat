package com.example.project_chat.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String uploadFile(MultipartFile file);

}
