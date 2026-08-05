# 📚 HComic - Backend REST API (Comic Reader Platform)

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-blue)
![Docker](https://img.shields.io/badge/Docker-blue)
![JWT](https://img.shields.io/badge/JWT-Security-red)

> Hệ thống Backend RESTful API cho ứng dụng đọc truyện tranh trực tuyến, phát triển bằng **Spring Boot 4** và **Java 21**, tích hợp bảo mật **JWT & RBAC**, tự động tối ưu hóa hình ảnh **WebP**, đóng gói **Docker** và tài liệu **Swagger UI**.

---

## 🚀 Key Highlights (Điểm Nổi Bật Kỹ Thuật)

- 🔒 **Stateless Security & RBAC**: Xác thực bằng JWT (`jjwt 0.12.6`) + Phân quyền chi tiết (`ROLE_ADMIN`, `ROLE_TRANSLATOR`, `ROLE_READER`) qua Spring Security & Method Security.
- 🖼️ **Image Optimization**: Tự động mã hóa & chuyển đổi hình ảnh tải lên (JPG/PNG/GIF) sang định dạng **WebP** giúp giảm mạnh dung lượng lưu trữ & tối ưu thời gian tải trang.
- 🐳 **Dockerization**: Cấu hình `Dockerfile` tối ưu với Multi-stage build (Eclipse Temurin JRE 21 & Spring Boot LayerTools) kết hợp `docker-compose` với PostgreSQL 17 (Healthcheck).
- 📖 **API Documentation**: Tích hợp Swagger / OpenAPI tự động sinh tài liệu API kèm cấu hình Bearer Authentication trực quan.

---

## 🛠️ Tech Stack

| Thành phần | Công nghệ / Thư viện |
| :--- | :--- |
| **Language & Framework** | Java 21, Spring Boot 4 |
| **Security** | Spring Security, JJWT (Stateless Auth) |
| **Database & ORM** | PostgreSQL 17, H2 (Dev/Test), Spring Data JPA, Hibernate |
| **Image Processing** | Sejda WebP ImageIO |
| **DevOps & Tools** | Docker, Docker Compose, Swagger UI, Lombok, Maven |

---

## 🌟 Core Features (Tính Năng Chính)

- **Authentication**: Đăng ký, Đăng nhập cấp phát JWT Access Token.
- **Quản lý Truyện & Chương**:
  - CRUD Truyện tranh & Chương truyện.
  - Tự động sinh `slug` chuẩn SEO cho Truyện & Chương.
  - Xử lý upload ảnh bìa & danh sách trang ảnh chương truyện (Multi-part upload).
- **Tương tác Người dùng**:
  - **Like/Unlike**: Thích hoặc bỏ thích truyện tranh.
  - **Rating**: Đánh giá số sao (1-5★) & tự động tính điểm đánh giá trung bình.
  - **Comments**: Bình luận theo truyện hoặc theo từng chương đọc cụ thể.

---

## ⚡ Quick Start (Khởi Chạy Nhanh)

### Cách 1: Chạy bằng Docker Compose (Nhanh nhất)
```bash
docker compose up --build -d
```
> API Server khởi chạy tại `http://localhost:8080` | Postgres 17 tại cổng `5432`.

### Cách 2: Chạy Local với Maven
1. Tạo tệp `.env` tại thư mục gốc:
   ```env
   DB_URL=jdbc:postgresql://localhost:5432/hcomic_db
   DB_USERNAME=postgres
   DB_PASSWORD=postgres
   JWT_SECRET=K9mP2xQ7nLs4Vz8RwH3tY6cFd1BaU5Je
   ```
2. Khởi chạy ứng dụng:
   ```bash
   ./mvnw spring-boot:run
   ```

---

## ⚡Test Account (Tài Khoản Test)

| Tài khoản | Role | Mật Khẩu |
| :--- | :--- |
| user | USER | 123456 |
| translator | TRANSLATOR | 123456 |
---

## 📖 API Documentation (Swagger)

Truy cập giao diện Swagger UI khi ứng dụng đang chạy:
👉 **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**

**Các bước thử nghiệm API cần xác thực:**
1. Gọi API `POST /api/auth/login` để lấy `accessToken`.
2. Bấm nút **Authorize** ➔ Dán `accessToken` vào ➔ Bấm **Authorize** ➔ **Close**.
