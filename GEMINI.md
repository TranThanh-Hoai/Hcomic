# HComic Backend

Java 21, Spring Boot, PostgreSQL, Redis, Flyway. Code theo package tính năng trong `src/main/java/com/comic/h/`.

## Lệnh (Windows; Linux/macOS dùng `./mvnw`)
- Chạy: `.\mvnw.cmd spring-boot:run`
- Test: `.\mvnw.cmd test`
- Build: `.\mvnw.cmd clean package -DskipTests`
- Docker: `docker compose up -d --build`

## Quy ước (dễ sai nếu không biết)
- Xóa Redis cache: luôn qua `SafeCacheEvictor.evictAfterCommit` (chỉ xóa sau khi DB commit, có try-catch để lỗi Redis không làm fail transaction).
- Schema DB chỉ đổi bằng Flyway (`src/main/resources/db/migration/V*__*.sql`); `ddl-auto=validate`.
- OSIV tắt: truy cập quan hệ lazy phải nằm trong `@Transactional`.
- Ảnh upload: luôn qua `ImageProcessor` (WebP, chất lượng 80%) rồi mới lưu vào `upload/`.
- Quyền sửa truyện: ngoài `@PreAuthorize`, phải gọi `ComicSecurityEvaluator.verifyOwnership`.
- Không trả Entity ra API: dùng DTO + MapStruct (`@Mapper(componentModel = "spring")`).
- OAuth2 & Stateless Auth: lưu authorization request tạm qua `HttpCookieOAuth2AuthorizationRequestRepository` (cookie mã hóa Base64), không dùng `HttpSession`. User OAuth2 không có password cục bộ (password nullable); `CustomUserDetailsService` phải fallback chuỗi rỗng `""` tránh `IllegalArgumentException` khi password null.

## Tài liệu
Cấu trúc, entrypoint, luồng chính: `docs/repo_map.md` (chỉ đọc khi cần).