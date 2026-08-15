-- Create genres table
CREATE TABLE IF NOT EXISTS genres (
    genre_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(120) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create comic_genres join table
CREATE TABLE IF NOT EXISTS comic_genres (
    comic_id BIGINT NOT NULL,
    genre_id BIGINT NOT NULL,
    PRIMARY KEY (comic_id, genre_id),
    CONSTRAINT fk_comic_genres_comic FOREIGN KEY (comic_id) REFERENCES comics(comic_id) ON DELETE CASCADE,
    CONSTRAINT fk_comic_genres_genre FOREIGN KEY (genre_id) REFERENCES genres(genre_id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_genres_slug ON genres(slug);
CREATE INDEX IF NOT EXISTS idx_comic_genres_comic_id ON comic_genres(comic_id);
CREATE INDEX IF NOT EXISTS idx_comic_genres_genre_id ON comic_genres(genre_id);

-- Insert 46 standard comic genres
INSERT INTO genres (name, slug, description, created_at, updated_at) VALUES
('Action', 'action', 'Thể loại hành động, kịch tính với các cảnh chiến đấu bắt mắt', NOW(), NOW()),
('Adventure', 'adventure', 'Thể loại phiêu lưu mạo hiểm, khám phá những vùng đất mới', NOW(), NOW()),
('Anime', 'anime', 'Truyện tranh được chuyển thể từ hoặc có phong cách Anime', NOW(), NOW()),
('Chuyển Sinh', 'chuyen-sinh', 'Nhân vật chết đi và đầu thai, tái sinh ở thế giới khác', NOW(), NOW()),
('Cổ Đại', 'co-dai', 'Bối cảnh thời xưa, phong kiến, cung đấu, kiếm hiệp', NOW(), NOW()),
('Comedy', 'comedy', 'Thể loại hài hước, mang lại tiếng cười và sự thư giãn', NOW(), NOW()),
('Comic', 'comic', 'Truyện tranh phong cách phương Tây (Marvel, DC,...)', NOW(), NOW()),
('Demons', 'demons', 'Thể loại có sự xuất hiện của ác quỷ, ma quỷ, quỷ giới', NOW(), NOW()),
('Detective', 'detective', 'Thể loại trinh thám, phá án, giải mã bí ẩn', NOW(), NOW()),
('Doujinshi', 'doujinshi', 'Truyện tự xuất bản do các fan sáng tác dựa trên tác phẩm gốc', NOW(), NOW()),
('Drama', 'drama', 'Thể loại tâm lý, kịch tính xoay quanh các mối quan hệ phức tạp', NOW(), NOW()),
('Fantasy', 'fantasy', 'Thế giới huyền bí, phép thuật, thần thoại và sinh vật kỳ ảo', NOW(), NOW()),
('Gender Bender', 'gender-bender', 'Nhân vật biến đổi giới tính hoặc giả trang giới tính khác', NOW(), NOW()),
('Harem', 'harem', 'Nhân vật chính được bao quanh bởi nhiều người khác giới yêu mến', NOW(), NOW()),
('Historical', 'historical', 'Truyện tranh có yếu tố lịch sử hoặc tái hiện các thời kỳ lịch sử', NOW(), NOW()),
('Horror', 'horror', 'Thể loại kinh dị, rùng rợn, giật gân, tạo cảm giác sợ hãi', NOW(), NOW()),
('Huyền Huyễn', 'huyen-huyen', 'Tiên hiệp, tu chân, pháp thuật thần bí theo phong cách phương Đông', NOW(), NOW()),
('Isekai', 'isekai', 'Nhân vật chính được dịch chuyển sang thế giới khác', NOW(), NOW()),
('Josei', 'josei', 'Thể loại hướng tới độc giả nữ trưởng thành', NOW(), NOW()),
('Mafia', 'mafia', 'Thế giới ngầm, tội phạm, xã hội đen', NOW(), NOW()),
('Magic', 'magic', 'Thế giới ma thuật, pháp sư, phù thủy', NOW(), NOW()),
('Manga', 'manga', 'Truyện tranh truyền thống xuất xứ từ Nhật Bản', NOW(), NOW()),
('Manhua', 'manhua', 'Truyện tranh có xuất xứ từ Trung Quốc', NOW(), NOW()),
('Manhwa', 'manhwa', 'Truyện tranh có xuất xứ từ Hàn Quốc', NOW(), NOW()),
('Martial Arts', 'martial-arts', 'Võ thuật, đấm bốc, quyền anh, kiếm đạo', NOW(), NOW()),
('Military', 'military', 'Quân sự, chiến tranh, vũ khí, chiến thuật chiến đấu', NOW(), NOW()),
('Mystery', 'mystery', 'Bí ẩn, các câu đố ly kỳ chưa có lời giải đáp', NOW(), NOW()),
('Ngôn Tình', 'ngon-tinh', 'Truyện tình cảm lãng mạn phong cách Trung Quốc', NOW(), NOW()),
('One shot', 'one-shot', 'Truyện ngắn chỉ gồm 1 chương duy nhất', NOW(), NOW()),
('Psychological', 'psychological', 'Tâm lý học, đấu trí, khai thác chiều sâu tâm lý nhân vật', NOW(), NOW()),
('Romance', 'romance', 'Tình cảm lãng mạn, tình yêu đôi lứa ngọt ngào', NOW(), NOW()),
('School Life', 'school-life', 'Đời sống học đường, tình bạn và tuổi học trò', NOW(), NOW()),
('Sci-fi', 'sci-fi', 'Khoa học viễn tưởng, không gian, robot, công nghệ tương lai', NOW(), NOW()),
('Seinen', 'seinen', 'Thể loại hướng tới độc giả nam giới trưởng thành', NOW(), NOW()),
('Shoujo', 'shoujo', 'Thể loại truyện tranh dành cho lứa tuổi thiếu nữ', NOW(), NOW()),
('Shoujo Ai', 'shoujo-ai', 'Tình cảm lãng mạn nhẹ nhàng giữa các nhân vật nữ', NOW(), NOW()),
('Shounen', 'shounen', 'Thể loại truyện tranh dành cho lứa tuổi thiếu niên, đề cao nhiệt huyết và tình bạn', NOW(), NOW()),
('Shounen Ai', 'shounen-ai', 'Tình cảm lãng mạn nhẹ nhàng giữa các nhân vật nam', NOW(), NOW()),
('Slice of life', 'slice-of-life', 'Lát cắt cuộc sống đời thường, nhẹ nhàng, sâu lắng', NOW(), NOW()),
('Sports', 'sports', 'Thể thao, thi đấu thể thao chuyên nghiệp và học đường', NOW(), NOW()),
('Supernatural', 'supernatural', 'Hiện tượng siêu nhiên, năng lực đặc biệt ngoài đời thực', NOW(), NOW()),
('Tragedy', 'tragedy', 'Bi kịch, những câu chuyện cảm động và kết thúc đượm buồn', NOW(), NOW()),
('Trọng Sinh', 'trong-sinh', 'Quay ngược thời gian trở về quá khứ để thay đổi số phận', NOW(), NOW()),
('Truyện Màu', 'truyen-mau', 'Truyện tranh được vẽ màu toàn bộ các trang', NOW(), NOW()),
('Webtoon', 'webtoon', 'Truyện tranh kỹ thuật số đọc cuộn dọc xuất xứ từ Hàn Quốc', NOW(), NOW()),
('Xuyên Không', 'xuyen-khong', 'Vượt không gian, thời gian sang một triều đại hoặc thế giới khác', NOW(), NOW())
ON CONFLICT (slug) DO NOTHING;
