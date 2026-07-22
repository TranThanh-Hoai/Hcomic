-- ==========================================
-- Flyway Migration: V1__init_schema.sql
-- Description: Create initial schema for Manga/Comic Reading Platform
-- Database Target: PostgreSQL 12+
-- ==========================================

-- ------------------------------------------
-- 1. Table: users
-- ------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    user_id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    avatar VARCHAR(512) DEFAULT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER' CHECK (role IN ('MEMBER', 'MODERATOR', 'ADMIN')),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'BANNED', 'PENDING')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE users IS 'Stores user account details';
COMMENT ON COLUMN users.user_id IS 'Primary key for user';
COMMENT ON COLUMN users.username IS 'Unique account name';
COMMENT ON COLUMN users.email IS 'Unique email address';

CREATE INDEX IF NOT EXISTS idx_users_role ON users (role);

-- ------------------------------------------
-- 2. Table: author
-- ------------------------------------------
CREATE TABLE IF NOT EXISTS author (
    author_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    slug VARCHAR(150) NOT NULL UNIQUE,
    biography TEXT DEFAULT NULL,
    country VARCHAR(50) DEFAULT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE author IS 'Stores author information';

CREATE INDEX IF NOT EXISTS idx_author_name ON author (name);

-- ------------------------------------------
-- 3. Table: artist
-- ------------------------------------------
CREATE TABLE IF NOT EXISTS artist (
    artist_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    slug VARCHAR(150) NOT NULL UNIQUE,
    biography TEXT DEFAULT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE artist IS 'Stores artist/illustrator information';

CREATE INDEX IF NOT EXISTS idx_artist_name ON artist (name);

-- ------------------------------------------
-- 4. Table: genre
-- ------------------------------------------
CREATE TABLE IF NOT EXISTS genre (
    genre_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255) DEFAULT NULL
);

COMMENT ON TABLE genre IS 'Stores comic genres/categories';

-- ------------------------------------------
-- 5. Table: scanlation_group
-- ------------------------------------------
CREATE TABLE IF NOT EXISTS scanlation_group (
    group_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL UNIQUE,
    description TEXT DEFAULT NULL,
    website VARCHAR(255) DEFAULT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE scanlation_group IS 'Stores translation/scanlation teams';

-- ------------------------------------------
-- 6. Table: comic
-- ------------------------------------------
CREATE TABLE IF NOT EXISTS comic (
    comic_id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    alternative_title VARCHAR(255) DEFAULT NULL,
    description TEXT DEFAULT NULL,
    cover_image VARCHAR(512) DEFAULT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ONGOING' CHECK (status IN ('ONGOING', 'COMPLETED', 'HIATUS', 'CANCELLED')),
    release_year INT DEFAULT NULL,
    view_count BIGINT NOT NULL DEFAULT 0 CHECK (view_count >= 0),
    follow_count INT NOT NULL DEFAULT 0 CHECK (follow_count >= 0),
    rating_average NUMERIC(3,2) NOT NULL DEFAULT 0.00 CHECK (rating_average BETWEEN 0.00 AND 5.00),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE comic IS 'Stores main comic metadata';

CREATE INDEX IF NOT EXISTS idx_comic_title ON comic (title);
CREATE INDEX IF NOT EXISTS idx_comic_status ON comic (status);
CREATE INDEX IF NOT EXISTS idx_comic_view_count ON comic (view_count DESC);
CREATE INDEX IF NOT EXISTS idx_comic_rating_avg ON comic (rating_average DESC);
CREATE INDEX IF NOT EXISTS idx_comic_created_at ON comic (created_at DESC);

-- ------------------------------------------
-- 7. Table: comic_author (Junction table N-N: Comic <-> Author)
-- ------------------------------------------
CREATE TABLE IF NOT EXISTS comic_author (
    comic_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    PRIMARY KEY (comic_id, author_id),
    CONSTRAINT fk_comic_author_comic FOREIGN KEY (comic_id) REFERENCES comic (comic_id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_comic_author_author FOREIGN KEY (author_id) REFERENCES author (author_id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- ------------------------------------------
-- 8. Table: comic_artist (Junction table N-N: Comic <-> Artist)
-- ------------------------------------------
CREATE TABLE IF NOT EXISTS comic_artist (
    comic_id BIGINT NOT NULL,
    artist_id BIGINT NOT NULL,
    PRIMARY KEY (comic_id, artist_id),
    CONSTRAINT fk_comic_artist_comic FOREIGN KEY (comic_id) REFERENCES comic (comic_id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_comic_artist_artist FOREIGN KEY (artist_id) REFERENCES artist (artist_id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- ------------------------------------------
-- 9. Table: comic_genre (Junction table N-N: Comic <-> Genre)
-- ------------------------------------------
CREATE TABLE IF NOT EXISTS comic_genre (
    comic_id BIGINT NOT NULL,
    genre_id INT NOT NULL,
    PRIMARY KEY (comic_id, genre_id),
    CONSTRAINT fk_comic_genre_comic FOREIGN KEY (comic_id) REFERENCES comic (comic_id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_comic_genre_genre FOREIGN KEY (genre_id) REFERENCES genre (genre_id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- ------------------------------------------
-- 10. Table: chapter
-- ------------------------------------------
CREATE TABLE IF NOT EXISTS chapter (
    chapter_id BIGSERIAL PRIMARY KEY,
    comic_id BIGINT NOT NULL,
    group_id BIGINT DEFAULT NULL,
    chapter_number NUMERIC(6,2) NOT NULL,
    title VARCHAR(255) DEFAULT NULL,
    slug VARCHAR(255) NOT NULL,
    view_count BIGINT NOT NULL DEFAULT 0 CHECK (view_count >= 0),
    upload_date TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_comic_chapter_num UNIQUE (comic_id, chapter_number),
    CONSTRAINT fk_chapter_comic FOREIGN KEY (comic_id) REFERENCES comic (comic_id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_chapter_group FOREIGN KEY (group_id) REFERENCES scanlation_group (group_id) ON DELETE SET NULL ON UPDATE CASCADE
);

COMMENT ON TABLE chapter IS 'Stores comic chapters';

CREATE INDEX IF NOT EXISTS idx_chapter_upload_date ON chapter (upload_date DESC);

-- ------------------------------------------
-- 11. Table: page
-- ------------------------------------------
CREATE TABLE IF NOT EXISTS page (
    page_id BIGSERIAL PRIMARY KEY,
    chapter_id BIGINT NOT NULL,
    page_number INT NOT NULL CHECK (page_number > 0),
    image_url VARCHAR(1024) NOT NULL,
    CONSTRAINT uk_chapter_page_num UNIQUE (chapter_id, page_number),
    CONSTRAINT fk_page_chapter FOREIGN KEY (chapter_id) REFERENCES chapter (chapter_id) ON DELETE CASCADE ON UPDATE CASCADE
);

COMMENT ON TABLE page IS 'Stores images/pages of a chapter';

-- ------------------------------------------
-- 12. Table: comment
-- ------------------------------------------
CREATE TABLE IF NOT EXISTS comment (
    comment_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    comic_id BIGINT NOT NULL,
    chapter_id BIGINT DEFAULT NULL,
    parent_comment_id BIGINT DEFAULT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_comment_comic FOREIGN KEY (comic_id) REFERENCES comic (comic_id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_comment_chapter FOREIGN KEY (chapter_id) REFERENCES chapter (chapter_id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_comment_parent FOREIGN KEY (parent_comment_id) REFERENCES comment (comment_id) ON DELETE CASCADE ON UPDATE CASCADE
);

COMMENT ON TABLE comment IS 'Stores comments for comics or chapters';

CREATE INDEX IF NOT EXISTS idx_comment_comic ON comment (comic_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_comment_chapter ON comment (chapter_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_comment_user ON comment (user_id);

-- ------------------------------------------
-- 13. Table: rating
-- ------------------------------------------
CREATE TABLE IF NOT EXISTS rating (
    rating_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    comic_id BIGINT NOT NULL,
    score SMALLINT NOT NULL CHECK (score BETWEEN 1 AND 5),
    rated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_comic_rating UNIQUE (user_id, comic_id),
    CONSTRAINT fk_rating_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_rating_comic FOREIGN KEY (comic_id) REFERENCES comic (comic_id) ON DELETE CASCADE ON UPDATE CASCADE
);

COMMENT ON TABLE rating IS 'Stores comic ratings by users';

-- ------------------------------------------
-- 14. Table: favorite (N-N: User <-> Comic)
-- ------------------------------------------
CREATE TABLE IF NOT EXISTS favorite (
    user_id BIGINT NOT NULL,
    comic_id BIGINT NOT NULL,
    added_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, comic_id),
    CONSTRAINT fk_favorite_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_favorite_comic FOREIGN KEY (comic_id) REFERENCES comic (comic_id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_favorite_comic ON favorite (comic_id);

-- ------------------------------------------
-- 15. Table: reading_history
-- ------------------------------------------
CREATE TABLE IF NOT EXISTS reading_history (
    history_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    comic_id BIGINT NOT NULL,
    last_chapter_id BIGINT NOT NULL,
    last_read_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_comic_history UNIQUE (user_id, comic_id),
    CONSTRAINT fk_history_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_history_comic FOREIGN KEY (comic_id) REFERENCES comic (comic_id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_history_chapter FOREIGN KEY (last_chapter_id) REFERENCES chapter (chapter_id) ON DELETE CASCADE ON UPDATE CASCADE
);

COMMENT ON TABLE reading_history IS 'Stores user reading progress and history';

CREATE INDEX IF NOT EXISTS idx_history_user_time ON reading_history (user_id, last_read_time DESC);
