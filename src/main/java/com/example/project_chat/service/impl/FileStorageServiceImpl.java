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
    private final String minioUrl;

    public FileStorageServiceImpl(MinioProperties minioProperties) {
        try {
            this.minioClient = MinioClient.builder()
                    .endpoint(minioProperties.getUrl())
                    .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                    .build();
            this.bucketName = minioProperties.getBucketName();
            this.minioUrl = minioProperties.getUrl();
        } catch (Exception e) {
            logger.error("Lỗi nghiêm trọng khi khởi tạo MinioClient", e);
            throw new RuntimeException("Không thể khởi tạo MinioClient", e);
        }
    }

    @Override
    public String uploadFile(MultipartFile file) {
        try {
            logger.info("Bắt đầu quá trình tải file. Kiểm tra bucket: '{}'", bucketName);

            // Kiem tra bucket
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            logger.info("Bucket '{}' có tồn tại không? -> {}", bucketName, found);

            if (!found) {
                logger.info("Bucket '{}' không tồn tại. Bắt đầu tạo mới...", bucketName);
                // Tao bucket moi
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                logger.info("Đã tạo bucket '{}' thành công.", bucketName);

                // cai dat policy
                String policyJson = createPublicReadPolicy(bucketName);
                minioClient.setBucketPolicy(
                        SetBucketPolicyArgs.builder()
                                .bucket(bucketName)
                                .config(policyJson)
                                .build()
                );
                logger.info("Đã cài đặt policy public-read cho bucket '{}'.", bucketName);
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
            return minioUrl + "/" + bucketName + "/" + uniqueFileName;
        } catch (Exception e) {
            logger.error("Lỗi nghiêm trọng trong quá trình tải file lên MinIO", e);
            throw new RuntimeException("Không thể tải file lên do lỗi server.", e);
        }
    }

    private String createPublicReadPolicy(String bucketName) {
        return "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:GetObject\"],\"Resource\":[\"arn:aws:s3:::" + bucketName + "/*\"]}]}";
    }
}