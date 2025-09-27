package com.example.project_chat.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Lớp cấu hình cho Swagger (OpenAPI) để kích hoạt chức năng Authorize với JWT.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        // Tên của Security Scheme, sẽ được hiển thị trong UI
        final String securitySchemeName = "Bearer Authentication";

        return new OpenAPI()
                // Thêm một "ổ khóa" vào tất cả các API, yêu cầu sử dụng Security Scheme ở trên
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(
                        // Định nghĩa Security Scheme
                        new Components()
                                .addSecuritySchemes(securitySchemeName,
                                        new SecurityScheme()
                                                .name(securitySchemeName)
                                                .type(SecurityScheme.Type.HTTP) // Kiểu: HTTP
                                                .scheme("bearer") // Scheme: bearer
                                                .bearerFormat("JWT") // Định dạng: JWT
                                )
                )
                // Thêm thông tin chung cho API
                .info(new Info().title("Project Chat API").version("1.0.0"));
    }
}