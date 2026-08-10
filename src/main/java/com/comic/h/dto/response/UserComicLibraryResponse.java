package com.comic.h.dto.response;

import java.time.LocalDateTime;
import com.comic.h.enums.ComicStatus;
import com.comic.h.enums.ShelfStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserComicLibraryResponse {
    private Long id;
    private Long comicId;
    private String comicTitle;
    private String comicSlug;
    private String coverImage;
    private String author;
    private ComicStatus comicStatus;
    private ShelfStatus status;
    private Long lastReadChapterId;
    private Double lastReadChapterNumber;
    private String lastReadChapterSlug;
    private Integer lastReadPageNumber;
    private Double lastReadPercentage;
    private LocalDateTime updatedAt;
}
