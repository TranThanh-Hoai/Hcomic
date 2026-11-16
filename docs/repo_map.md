# Repository Map - HComic Backend

## 1. Cấu trúc thư mục chính
- `.github/workflows`: Kịch bản CI/CD GitHub Actions tự động chạy unit test và build/push Docker image lên Docker Hub.
- `.mvn`: Wrapper Maven và cấu hình thực thi môi trường không cần cài trước Maven.
- `docs`: Tài liệu kỹ thuật dự án và tài nguyên hình ảnh minh họa (`docs/images`).
- `upload`: Thư mục lưu trữ tệp tĩnh trên đĩa cứng (gồm `upload/comic` cho ảnh bìa và `upload/chapter` cho ảnh chương).
- `src/main/resources`: Cấu hình ứng dụng (`application.properties`) và các file migration schema Flyway (`db/migration`).
- `src/main/java/com/comic/h/analytics`: Nghiệp vụ thống kê dashboard admin, tăng trưởng người dùng 30 ngày và truyện thịnh hành.
- `src/main/java/com/comic/h/comic`: Nghiệp vụ lõi quản lý truyện, thể loại, chương (chapter), xử lý upload nén ảnh WebP và lọc/tìm kiếm.
- `src/main/java/com/comic/h/common`: Cấu hình dùng chung gồm Spring Security JWT, Redis cache, WebSocket STOMP, Global Exception Handler và lưu trữ file.
- `src/main/java/com/comic/h/identity`: Xác thực người dùng (đăng ký, đăng nhập mật khẩu & Google OAuth2), phân quyền, xoay vòng Refresh Token và scheduler dọn token hết hạn. Package `identity/security/oauth2` chứa toàn bộ luồng OAuth2 client (stateless cookie repository, user info factory, custom user service, success/failure handler).
- `src/main/java/com/comic/h/interaction`: Tương tác bạn đọc gồm thích (like), đánh giá sao (rate), bình luận và phát sự kiện bình luận real-time qua WebSocket.
- `src/main/java/com/comic/h/library`: Tủ sách cá nhân của user gồm lịch sử đọc, kệ truyện theo dõi và đánh dấu trang đọc dở.
- `src/main/java/com/comic/h/moderation`: Báo cáo vi phạm nội dung (truyện, bình luận) và nghiệp vụ kiểm duyệt/khóa tài khoản của admin.
- `src/test/java/com/comic/h`: Bộ unit test và integration test cho service, bảo mật, cấu hình Redis, WebSocket và exception handler.

## 2. Entrypoint
- Main: `HApplication.java` (`@EnableScheduling`)
- REST: controller nằm trong package của từng tính năng, URL dạng `/api/<tính năng>/**` (admin: `/api/admin/**`). Lưu ý: `ComicController` cũng phục vụ đọc chương; `ChapterImageController` lo upload ảnh chương.
- OAuth2: Khởi tạo `/oauth2/authorization/{provider}` (ví dụ: `/oauth2/authorization/google`), callback nhận mã code `/login/oauth2/code/{provider}`.
- WebSocket: `common/config/WebSocketConfig.java` (endpoint `/ws`, nhận `/app/**`, phát `/topic/**`)
- Scheduler: `identity/scheduler/RefreshTokenScheduler.java` (dọn refresh token hết hạn lúc 03:00 mỗi ngày)

## 3. Các luồng xử lý chính

### Luồng 1: Đăng nhập (Authentication & JWT Issuance)
`AuthController.java` -> `AuthServiceImpl.java` -> `AuthenticationManager` & `JwtTokenProvider.java` & `RefreshTokenServiceImpl.java` -> `UserRepository.java`
- Xác thực username/password, tạo cặp Access Token (JWT) & Refresh Token (lưu DB), trả về `AuthResponse`.

### Luồng 2: Đọc chi tiết chương truyện (Read Chapter by Slug & View Count)
`ComicController.java` -> `ChapterServiceImpl.java` -> `ChapterRepository.java` & `ComicRepository.java` -> `ChapterMapper.java`
- Đọc thông tin chương từ cache Redis (`chapter_detail`) hoặc DB, điều hướng slug chương trước/sau, tăng view count chương/truyện và trả về danh sách ảnh.

### Luồng 3: Tải ảnh chương truyện hàng loạt (Batch Upload Images with WebP Conversion)
`JwtAuthenticationFilter.java` -> `ChapterImageController.java` -> `ChapterImageServiceImpl.java` -> `ComicSecurityEvaluator.java` & `ImageProcessor.java` & `LocalStorageServiceImpl.java` -> `ChapterImageRepository.java` & `SafeCacheEvictor.java`
- Kiểm tra quyền uploader của truyện, chuyển đổi file ảnh sang WebP chất lượng 80%, ghi tệp vào `upload/chapter/`, lưu metadata vào DB và xóa cache chương an toàn sau khi commit.

### Luồng 4: Đăng nhập mạng xã hội (OAuth2 Social Login - Google)
`SecurityConfig.java` -> `HttpCookieOAuth2AuthorizationRequestRepository.java` -> Google OAuth2 (`profile,email`) -> `CustomOAuth2UserService.java` & `OAuth2AuthenticationSuccessHandler.java` -> `JwtTokenProvider.java` & `RefreshTokenServiceImpl.java` -> Redirect target URI
- Chuyển hướng xác thực qua Google bằng cơ chế Stateless Cookie, tự động tạo/liên kết tài khoản trong DB (`users.provider = 'GOOGLE'`, password nullable), cấp cặp Access Token JWT & Refresh Token rồi redirect an toàn về Frontend/Target URI (kèm `token` & `refreshToken` trên query param).