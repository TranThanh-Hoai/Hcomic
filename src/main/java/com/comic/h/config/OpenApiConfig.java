package com.comic.h.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "HComic API", version = "1.0.0", description = "REST API cho ứng dụng HComic.\n\n"
                +
                "### Tài khoản test:\n" +

                "Tài khoản test USER:\n" +
                "- \"username\": \"user\",\n" +
                "- \"password\": \"123456\"\n\n" +

                "Tài khoản test TRANSLATOR:\n\n" +
                "- \"username\": \"translator\",\n" +
                "- \"password\": \"123456\"\n" +
                "\n" +
                "### 🔑 Hướng dẫn xác thực JWT:\n" +
                "1. Gọi API `POST /api/auth/login` (hoặc `POST /api/auth/register`) bên dưới để lấy `accessToken`.(Có thể lấy tài khoản test phía trên để dùng)\n"
                +
                "2. Click vào nút **Authorize** màu xanh ở góc trên bên phải.\n" +
                "3. Dán trực tiếp chuỗi token vừa lấy được vào ô Value (không cần thêm tiền tố 'Bearer ').\n" +
                "4. Nhấp nút **Authorize** -> **Close** để thực thi các API bảo vệ."
                ),
                 security = @SecurityRequirement(name = "bearerAuth")
        )

        @SecurityScheme(name = "bearerAuth", 
                        type = SecuritySchemeType.HTTP, 
                        scheme = "bearer", 
                        bearerFormat = "JWT", 
                        description = "Dán chuỗi accessToken thu được từ API /api/auth/login vào đây để xác thực."
                )

public class OpenApiConfig {
}
