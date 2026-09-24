package com.comic.h.comic.service.impl;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comic.h.comic.dto.request.ChapterRequest;
import com.comic.h.comic.dto.response.ChapterDetailResponse;
import com.comic.h.comic.dto.response.ChapterResponse;
import com.comic.h.comic.entity.Chapter;
import com.comic.h.comic.entity.ChapterImage;
import com.comic.h.comic.entity.Comic;
import com.comic.h.comic.mapper.ChapterMapper;
import com.comic.h.comic.repository.ChapterRepository;
import com.comic.h.comic.repository.ComicRepository;
import com.comic.h.comic.security.ComicSecurityEvaluator;
import com.comic.h.comic.service.ChapterService;
import com.comic.h.common.exception.BadRequestException;
import com.comic.h.common.exception.ResourceNotFoundException;
import com.comic.h.common.storage.FileStorageService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChapterServiceImpl implements ChapterService {

    @Value("${app.upload.comic-dir:upload/comic}")
    private String comicUploadDir = "upload/comic";

    private final ChapterRepository chapterRepository;
    private final ComicRepository comicRepository;
    private final FileStorageService fileStorageService;
    private final ComicSecurityEvaluator comicSecurityEvaluator;
    private final ChapterMapper chapterMapper;
    private final CacheManager cacheManager;

    @Autowired
    @Lazy
    private ChapterService self;

    @Override
    @Transactional
    public ChapterResponse createChapter(Long comicId, ChapterRequest request) {
        Comic comic = comicRepository.findById(comicId)
                .orElseThrow(() -> new ResourceNotFoundException("Comic not found with id: " + comicId));

        comicSecurityEvaluator.verifyOwnership(comic);

        if (request.getChapterNumber() == null) {
            throw new BadRequestException("Chapter number is required");
        }

        if (chapterRepository.existsByComicIdAndChapterNumber(comicId, request.getChapterNumber())) {
            throw new BadRequestException("Chapter number " + formatChapterNumber(request.getChapterNumber()) + " already exists for this comic");
        }

        String chapterNumStr = formatChapterNumber(request.getChapterNumber());
        String slug = "chuong-" + chapterNumStr;

        String title = request.getTitle();
        if (title == null || title.trim().isEmpty()) {
            title = "Chương " + chapterNumStr;
        }

        Chapter chapter = Chapter.builder()
                .comic(comic)
                .chapterNumber(request.getChapterNumber())
                .title(title)
                .slug(slug)
                .viewCount(0L)
                .uploadStatus("PENDING")
                .images(new ArrayList<>())
                .build();

        Chapter savedChapter = chapterRepository.save(chapter);
        evictComicAndChapterCaches(comic.getSlug());
        return chapterMapper.toResponse(savedChapter);
    }

    @Override
    @Transactional(readOnly = true)
    public ChapterResponse getChapterById(Long chapterId) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found with id: " + chapterId));
        return chapterMapper.toResponse(chapter);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "chapters_list", key = "#comicSlug + ':' + (#sort != null ? #sort.toLowerCase() : 'desc')", sync = true)
    public List<ChapterResponse> getChaptersByComicSlug(String comicSlug, String sort) {
        if (!comicRepository.existsBySlug(comicSlug)) {
            throw new ResourceNotFoundException("Comic not found with slug: " + comicSlug);
        }

        List<Chapter> chapters;
        if ("asc".equalsIgnoreCase(sort)) {
            chapters = chapterRepository.findByComicSlugOrderByChapterNumberAsc(comicSlug);
        } else {
            chapters = chapterRepository.findByComicSlugOrderByChapterNumberDesc(comicSlug);
        }

        return chapters.stream()
                .map(chapterMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChapterResponse> getChaptersByComicId(Long comicId, String sort) {
        if (!comicRepository.existsById(comicId)) {
            throw new ResourceNotFoundException("Comic not found with id: " + comicId);
        }

        List<Chapter> chapters;
        if ("asc".equalsIgnoreCase(sort)) {
            chapters = chapterRepository.findByComicIdOrderByChapterNumberAsc(comicId);
        } else {
            chapters = chapterRepository.findByComicIdOrderByChapterNumberDesc(comicId);
        }

        return chapters.stream()
                .map(chapterMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "chapter_detail", key = "#comicSlug + ':' + #chapterSlug", sync = true)
    public ChapterDetailResponse getCachedChapterDetail(String comicSlug, String chapterSlug) {
        Chapter chapter = chapterRepository.findByComicSlugAndSlug(comicSlug, chapterSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found with slug: " + chapterSlug + " for comic: " + comicSlug));

        Comic comic = chapter.getComic();
        Optional<Chapter> prevChapterOpt = chapterRepository
                .findFirstByComicIdAndChapterNumberLessThanOrderByChapterNumberDesc(comic.getId(), chapter.getChapterNumber());
        Optional<Chapter> nextChapterOpt = chapterRepository
                .findFirstByComicIdAndChapterNumberGreaterThanOrderByChapterNumberAsc(comic.getId(), chapter.getChapterNumber());

        long currentViewCount = chapter.getViewCount() != null ? chapter.getViewCount() : 0L;
        String prevSlug = prevChapterOpt.map(Chapter::getSlug).orElse(null);
        String nextSlug = nextChapterOpt.map(Chapter::getSlug).orElse(null);

        return chapterMapper.toDetailResponse(chapter, prevSlug, nextSlug, currentViewCount);
    }

    @Override
    @Transactional
    public ChapterDetailResponse getChapterDetailBySlug(String comicSlug, String chapterSlug) {
        ChapterDetailResponse detail = (self != null ? self : this).getCachedChapterDetail(comicSlug, chapterSlug);

        chapterRepository.incrementViewCount(detail.getId());
        if (detail.getComicId() != null) {
            comicRepository.incrementViewCount(detail.getComicId());
        }

        Long latestViewCount = chapterRepository.findViewCountById(detail.getId());
        detail.setViewCount(latestViewCount != null ? latestViewCount : ((detail.getViewCount() != null ? detail.getViewCount() : 0L) + 1));

        return detail;
    }

    @Override
    @Transactional
    public ChapterResponse updateChapter(Long chapterId, ChapterRequest request) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found with id: " + chapterId));

        comicSecurityEvaluator.verifyOwnership(chapter.getComic());

        String oldSlug = chapter.getSlug();
        String comicSlug = chapter.getComic().getSlug();

        if (request.getChapterNumber() != null && !request.getChapterNumber().equals(chapter.getChapterNumber())) {
            if (chapterRepository.existsByComicIdAndChapterNumberAndIdNot(chapter.getComic().getId(), request.getChapterNumber(), chapterId)) {
                throw new BadRequestException("Chapter number " + formatChapterNumber(request.getChapterNumber()) + " already exists for this comic");
            }
            chapter.setChapterNumber(request.getChapterNumber());
            chapter.setSlug("chuong-" + formatChapterNumber(request.getChapterNumber()));
        }

        if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
            chapter.setTitle(request.getTitle());
        }

        Chapter updatedChapter = chapterRepository.save(chapter);
        evictChapterDetailCache(comicSlug, oldSlug);
        if (!oldSlug.equals(updatedChapter.getSlug())) {
            evictChapterDetailCache(comicSlug, updatedChapter.getSlug());
        }
        evictComicAndChapterCaches(comicSlug);
        return chapterMapper.toResponse(updatedChapter);
    }

    @Override
    @Transactional
    public void deleteChapter(Long chapterId) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found with id: " + chapterId));

        comicSecurityEvaluator.verifyOwnership(chapter.getComic());

        List<String> filePaths = chapter.getImages() != null ? chapter.getImages().stream()
                .map(ChapterImage::getImagePath)
                .toList() : List.of();

        Comic comic = chapter.getComic();
        String chapterNumStr = formatChapterNumber(chapter.getChapterNumber());
        String chapterDirName = comic.getSlug() + "-chapter-" + chapterNumStr;
        Path chapterDir = Paths.get(comicUploadDir, comic.getSlug(), chapterDirName);

        fileStorageService.scheduleFileCleanupOnCommit(filePaths, null);
        fileStorageService.scheduleDirectoryCleanupOnCommit(chapterDir.toString().replace('\\', '/'));

        evictChapterDetailCache(comic.getSlug(), chapter.getSlug());
        evictComicAndChapterCaches(comic.getSlug());
        chapterRepository.delete(chapter);
    }

    private void evictChapterDetailCache(String comicSlug, String chapterSlug) {
        if (cacheManager != null && comicSlug != null && chapterSlug != null) {
            var cache = cacheManager.getCache("chapter_detail");
            if (cache != null) {
                cache.evict(comicSlug + ":" + chapterSlug);
            }
        }
    }

    private void evictComicAndChapterCaches(String comicSlug) {
        if (cacheManager != null && comicSlug != null) {
            var chaptersCache = cacheManager.getCache("chapters_list");
            if (chaptersCache != null) {
                chaptersCache.evict(comicSlug + ":asc");
                chaptersCache.evict(comicSlug + ":desc");
            }
            var comicCache = cacheManager.getCache("comic_detail");
            if (comicCache != null) {
                comicCache.evict(comicSlug);
            }
            var pageCache = cacheManager.getCache("comics_page");
            if (pageCache != null) {
                pageCache.clear();
            }
        }
    }

    private String formatChapterNumber(Double chapterNumber) {
        if (chapterNumber == null) {
            return "0";
        }
        if (chapterNumber == chapterNumber.longValue()) {
            return String.valueOf(chapterNumber.longValue());
        }
        return String.valueOf(chapterNumber);
    }

    @Override
    public Chapter getChapterEntityById(Long chapterId) {
        return chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found with id: " + chapterId));
    }

    @Override
    public void validateChapterBelongsToComic(Chapter chapter, Comic comic) {
        if (chapter == null || comic == null || chapter.getComic() == null || !chapter.getComic().getId().equals(comic.getId())) {
            throw new BadRequestException("Chapter does not belong to the requested comic");
        }
    }
}

