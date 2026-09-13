package com.comic.h.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "HComic API", version = "1.0.0", description = "RESTful API for the HComic reader platform.\n\n"
        + "### Test Accounts:\n"
        + "- **USER**: username `user` / password `123456`\n"
        + "- **TRANSLATOR**: username `translator` / password `123456`\n\n"
        + "### 🔑 JWT Authentication Guide:\n"
        + "1. Call `POST /api/auth/login` (or `POST /api/auth/register`) to obtain an `accessToken`.\n"
        + "2. Click the green **Authorize** button at the top right.\n"
        + "3. Paste the token value into the Value input field (without the 'Bearer ' prefix).\n"
        + "4. Click **Authorize** -> **Close** to execute protected endpoints."), security = @SecurityRequirement(name = "bearerAuth"))

@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT", description = "Paste the accessToken obtained from /api/auth/login here to authenticate.")

public class OpenApiConfig {
}
