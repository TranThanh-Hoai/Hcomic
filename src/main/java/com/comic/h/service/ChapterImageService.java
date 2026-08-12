package com.comic.h.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.comic.h.dto.response.ChapterImageResponse;

public interface ChapterImageService {

    ChapterImageResponse uploadOrReplaceImage(Long chapterId, MultipartFile image, Integer pageNumber);

    List<ChapterImageResponse> uploadImagesBatch(Long chapterId, List<MultipartFile> images, Integer startPageNumber);

    int countImages(Long chapterId);

    List<ChapterImageResponse> getChapterImages(Long chapterId);

    void deleteImageByPageNumber(Long chapterId, Integer pageNumber);

    long deleteAllImages(Long chapterId);
}
