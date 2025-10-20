package com.example.project_chat.service.impl;

import com.example.project_chat.config.MinioProperties;
import com.example.project_chat.service.FileStorageService;
import io.minio.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageServiceImpl.class);
    private final MinioClient minioClient;
    private final String bucketName;
    private final String externalUrl; // Sửa tên biến này

    public FileStorageServiceImpl(MinioProperties minioProperties) {
        try {
            this.minioClient = MinioClient.builder()
                    .endpoint(minioProperties.getUrl()) // URL nội bộ để kết nối
                    .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                    .build();
            this.bucketName = minioProperties.getBucketName();
            // Lấy URL công khai để trả về cho client
            this.externalUrl = minioProperties.getExternalUrl(); // Gán URL công khai
        } catch (Exception e) {
            logger.error("Lỗi nghiêm trọng khi khởi tạo MinioClient", e);
            throw new RuntimeException("Không thể khởi tạo MinioClient", e);
        }
    }

    @Override
    public String uploadFile(MultipartFile file) {
        try {
            // (Phần logic kiểm tra bucket và tạo bucket giữ nguyên)
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                String policyJson = createPublicReadPolicy(bucketName);
                minioClient.setBucketPolicy(
                        SetBucketPolicyArgs.builder()
                                .bucket(bucketName)
                                .config(policyJson)
                                .build()
                );
            }

            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(uniqueFileName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            logger.info("Đã tải file '{}' lên bucket '{}' thành công.", uniqueFileName, bucketName);
            // SỬA DÒNG NÀY: Dùng externalUrl để tạo đường dẫn trả về
            return externalUrl + "/" + bucketName + "/" + uniqueFileName;
        } catch (Exception e) {
            logger.error("Lỗi nghiêm trọng trong quá trình tải file lên MinIO", e);
            throw new RuntimeException("Không thể tải file lên do lỗi server.", e);
        }
    }

    private String createPublicReadPolicy(String bucketName) {
        return "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:GetObject\"],\"Resource\":[\"arn:aws:s3:::" + bucketName + "/*\"]}]}";
    }
}