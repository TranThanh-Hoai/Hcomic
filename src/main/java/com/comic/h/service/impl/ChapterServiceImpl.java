package com.comic.h.service.impl;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comic.h.dto.request.ChapterRequest;
import com.comic.h.dto.response.ChapterDetailResponse;
import com.comic.h.dto.response.ChapterResponse;
import com.comic.h.entity.Chapter;
import com.comic.h.entity.ChapterImage;
import com.comic.h.entity.Comic;
import com.comic.h.exception.BadRequestException;
import com.comic.h.exception.ResourceNotFoundException;
import com.comic.h.mapper.ChapterMapper;
import com.comic.h.repository.ChapterRepository;
import com.comic.h.repository.ComicRepository;
import com.comic.h.security.ComicSecurityEvaluator;
import com.comic.h.service.ChapterService;
import com.comic.h.service.FileStorageService;

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
    @Transactional
    public ChapterDetailResponse getChapterDetailBySlug(String comicSlug, String chapterSlug) {
        Chapter chapter = chapterRepository.findByComicSlugAndSlug(comicSlug, chapterSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found with slug: " + chapterSlug + " for comic: " + comicSlug));

        chapterRepository.incrementViewCount(chapter.getId());

        Comic comic = chapter.getComic();
        comicRepository.incrementViewCount(comic.getId());

        Optional<Chapter> prevChapterOpt = chapterRepository
                .findFirstByComicIdAndChapterNumberLessThanOrderByChapterNumberDesc(comic.getId(), chapter.getChapterNumber());
        Optional<Chapter> nextChapterOpt = chapterRepository
                .findFirstByComicIdAndChapterNumberGreaterThanOrderByChapterNumberAsc(comic.getId(), chapter.getChapterNumber());

        long updatedViewCount = (chapter.getViewCount() != null ? chapter.getViewCount() : 0L) + 1;
        String prevSlug = prevChapterOpt.map(Chapter::getSlug).orElse(null);
        String nextSlug = nextChapterOpt.map(Chapter::getSlug).orElse(null);

        return chapterMapper.toDetailResponse(chapter, prevSlug, nextSlug, updatedViewCount);
    }

    @Override
    @Transactional
    public ChapterResponse updateChapter(Long chapterId, ChapterRequest request) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found with id: " + chapterId));

        comicSecurityEvaluator.verifyOwnership(chapter.getComic());

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

        chapterRepository.delete(chapter);
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
}
