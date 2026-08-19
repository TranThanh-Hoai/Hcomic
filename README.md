# 📚 HComic - Backend REST API (Comic Reader Platform)

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21" />
  <img src="https://img.shields.io/badge/Spring_Boot-4.1.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot 4" />
  <img src="https://img.shields.io/badge/PostgreSQL-17-4169E1?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL 17" />
  <img src="https://img.shields.io/badge/Flyway-DB_Migration-CC0200?style=for-the-badge&logo=flyway&logoColor=white" alt="Flyway" />
  <img src="https://img.shields.io/badge/Docker-Container-2496ED?style=for-the-badge&logo=docker&logoColor=white" alt="Docker" />
  <img src="https://img.shields.io/badge/JWT-JJWT_0.12.6-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white" alt="JWT" />
  <img src="https://img.shields.io/badge/Swagger-OpenAPI_3.0-85EA2D?style=for-the-badge&logo=swagger&logoColor=black" alt="Swagger" />
  <img src="https://img.shields.io/badge/CI%2FCD-GitHub_Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white" alt="GitHub Actions" />
</p>

> A Backend RESTful API for an online comic reader platform built with **Spring Boot 4** and **Java 21**. Features **JWT & RBAC security**, automatic **WebP image conversion**, database query optimization (solving **N+1 queries**), schema migrations with **Flyway**, and **Docker & CI/CD** integration.

---

## 🌟 Features

- 🔐 **Authentication & RBAC**: Register, Login, Refresh Token, and Role-Based Access Control (`ROLE_ADMIN`, `ROLE_TRANSLATOR`, `ROLE_USER`).
- 📖 **Comics & Chapters**: CRUD comics, multi-genre tagging, auto SEO slugs, multi-image upload with automatic conversion to **WebP (Quality 80%)**.
- 🔍 **Search & Catalog**: Fast keyword search and multi-criteria filters (by genre, status, uploader, views, rating).
- 💬 **Community & Interaction**: Likes, 1-5 star ratings, comments on comics & chapters, and user report moderation queue.
- 📚 **Library & History**: Custom reading shelves (`READING`, `COMPLETED`, etc.), auto reading progress tracker, and page bookmarks.
- 🛡️ **Admin Dashboard**: System overview stats, 30-day user growth analytics, trending comics, and user ban/unban moderation.

---

## 🛠️ Tech Stack

| Category | Technology / Library | Description |
| :--- | :--- | :--- |
| **Language & Framework** | Java 21, Spring Boot 4.1.0 | Core backend platform |
| **Database & Migration** | PostgreSQL 17, Flyway, H2 (Test) | Database, schema versioning, and testing |
| **Security** | Spring Security, JJWT (0.12.6) | Stateless JWT Access & Refresh tokens |
| **Image Processing** | Sejda WebP ImageIO (0.1.6) | Automatic WebP compression |
| **API & DevOps** | Swagger UI (OpenAPI 3), Docker, GitHub Actions | API docs, containerization, and CI/CD |

---

## 📊 Performance Benchmark

Tested with **10 concurrent workers / 50 requests per test** in a local environment:

| Business Domain | Scenario / API Endpoint | Baseline Latency | Optimized Latency | Latency Reduction | Baseline RPS | Optimized RPS | Throughput Change |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **1. Catalog & Discovery** | Catalog Filter & Pagination | 85.61 ms | 60.67 ms | **-29.1%** | 109.0 req/s | 155.2 req/s | **1.42x** |
| **1. Catalog & Discovery** | Quick Keyword Search | 45.75 ms | 35.44 ms | **-22.5%** | 197.8 req/s | 252.6 req/s | **1.28x** |
| **3. Community & Comments** | Comic Comments Feed | 39.77 ms | 28.65 ms | **-28.0%** | 223.7 req/s | 282.7 req/s | **1.26x** |
| **3. Community & Ratings** | Comic Rating Average | 21.26 ms | 18.87 ms | **-11.2%** | 399.6 req/s | 447.0 req/s | **1.12x** |
| **4. Library & Tracking** | User Comic Library | 173.05 ms | 43.16 ms | **-75.1%** | 56.4 req/s | 209.0 req/s | **3.70x** |
| **4. Library & Tracking** | User Reading History | 117.31 ms | 34.79 ms | **-70.3%** | 81.8 req/s | 259.8 req/s | **3.17x** |
| **5. Admin & Analytics** | Admin User Growth (30 Days) | 96.30 ms | 23.70 ms | **-75.4%** | 98.5 req/s | 337.7 req/s | **3.43x** |
| **5. Admin & Analytics** | Admin User Search & Pagination | 31.67 ms | 28.84 ms | **-8.9%** | 293.2 req/s | 325.1 req/s | **1.11x** |

### 📈 Comparison Chart

![HComic Benchmark Comparison](docs/images/benchmark_comparison.png)

*The top chart compares **Response Latency** in ms (shorter/green is better), and the bottom chart compares **Throughput (RPS)** (longer/green is better). The biggest improvements are in **User Comic Library** (3.70x faster), **Reading History** (3.17x faster), and **User Growth Analytics** (3.43x faster) by fixing N+1 queries and optimizing SQL aggregates.*

> 💡 *Note: The benchmark was conducted in a local development environment using Python scripts generated with the assistance of the **Antigravity AI Agent**.*

---

## ⚡ Quick Start

### 1. Create `.env` File
Create a `.env` file in the project root directory (or copy from `.env.example`):

```env
DB_URL=jdbc:postgresql://localhost:5432/hcomic_db
DB_USERNAME=postgres
DB_PASSWORD=postgres
JWT_SECRET=K9mP2xQ7nLs4Vz8RwH3tY6cFd1BaU5Je
```

---

### 2. Run the Application

#### Option A: Run with Docker Compose (Recommended)
Starts both the Spring Boot API and PostgreSQL 17 container:

```bash
docker compose up --build -d
```
- API Server: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

To stop the containers:
```bash
docker compose down
```

#### Option B: Run Locally with Maven
Ensure PostgreSQL is running locally, then start the application:

```bash
# Linux / macOS
./mvnw spring-boot:run

# Windows (PowerShell / CMD)
.\mvnw.cmd spring-boot:run
```

---

## 👥 Test Accounts

Pre-configured accounts for testing different roles:

| Username | Password | Role | Description |
| :--- | :--- | :--- | :--- |
| `user` | `123456` | `ROLE_USER` | Read comics, like, rate, comment, manage library & bookmarks, report content |
| `translator` | `123456` | `ROLE_TRANSLATOR` | User features + upload and manage comics, chapters, and chapter images |
---

## 📖 API Documentation (Swagger UI)

Explore and test all API endpoints interactively via Swagger UI:

👉 **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**

**How to authenticate in Swagger UI:**
1. Call `POST /api/auth/login` with credentials above to get an `accessToken`.
2. Click the **Authorize** button (top right).
3. Paste the token into the Value box and click **Authorize** ➔ **Close**.

---

## 🧪 Testing & CI/CD

- **Run Unit Tests locally**:
  ```bash
  ./mvnw test        # Linux/macOS
  .\mvnw.cmd test    # Windows
  ```
- **GitHub Actions CI/CD**:
  - `test.yml`: Runs 22 unit tests automatically on Pull Requests to `develop`.
  - `deloy-docker-hub.yml`: Builds and pushes the Docker image to Docker Hub when pushed to `main`.

---

<p align="center">
  Developed with ❤️ by <b>TranThanh-Hoai</b>
</p>
