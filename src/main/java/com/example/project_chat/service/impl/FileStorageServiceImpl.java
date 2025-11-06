package com.example.project_chat.service.impl;

import com.example.project_chat.service.FileStorageService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;

import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

private static final Logger logger = LoggerFactory.getLogger(FileStorageServiceImpl.class);

    // Inject S3 bucket name và region từ application.yml
    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${cloud.aws.region.static}") // Lấy region từ cấu hình AWS
    private String region;

    private S3Client s3Client;

    // Sử dụng @PostConstruct để khởi tạo S3Client sau khi các dependency được inject
    @PostConstruct
    public void init() {
        try {
            // Cấu hình S3Client
            // DefaultCredentialsProvider sẽ tự động tìm credentials theo chuỗi mặc định:
            // 1. Biến môi trường (AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY)
            // 2. Java system properties (aws.accessKeyId, aws.secretKey)
            // 3. File credentials (~/.aws/credentials)
            // 4. ECS container credentials (nếu chạy trên ECS)
            // 5. EC2 instance profile credentials (nếu chạy trên EC2)
            this.s3Client = S3Client.builder()
                    .region(Region.of(region)) // Sử dụng region đã inject
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .build();
            logger.info("S3Client được khởi tạo thành công cho region: {} và bucket: {}", region, bucketName);
        } catch (Exception e) {
            logger.error("Lỗi nghiêm trọng khi khởi tạo S3Client", e);
            throw new RuntimeException("Không thể khởi tạo S3Client", e);
        }
    }


    @Override
    public String uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống.");
        }

        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension; // Key (tên object trong S3)

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(uniqueFileName)
                    .contentType(file.getContentType())
                    .build();

            // Upload file
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            logger.info("Đã tải file '{}' lên S3 bucket '{}' thành công.", uniqueFileName, bucketName);

            String fileUrl = s3Client.utilities().getUrl(GetUrlRequest.builder()
                    .bucket(bucketName)
                    .key(uniqueFileName)
                    .build()).toString();
            // Nếu region là us-east-1, URL không có phần region
            if ("us-east-1".equals(region)) {
                fileUrl = String.format("https://%s.s3.amazonaws.com/%s", bucketName, uniqueFileName);
            }
            return fileUrl;

        } catch (IOException e) {
            logger.error("Lỗi I/O khi đọc file đầu vào", e);
            throw new RuntimeException("Lỗi đọc file.", e);
        } catch (Exception e) {
            logger.error("Lỗi nghiêm trọng trong quá trình tải file lên S3", e);
            throw new RuntimeException("Không thể tải file lên do lỗi server.", e);
        }
    }
}