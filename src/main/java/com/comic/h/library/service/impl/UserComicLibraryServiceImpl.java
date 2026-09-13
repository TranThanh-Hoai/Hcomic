package com.comic.h.library.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comic.h.comic.entity.Comic;
import com.comic.h.comic.service.ComicService;
import com.comic.h.identity.entity.User;
import com.comic.h.identity.service.UserService;
import com.comic.h.library.dto.request.LibraryStatusRequest;
import com.comic.h.library.dto.response.UserComicLibraryResponse;
import com.comic.h.library.entity.ReadingHistory;
import com.comic.h.library.entity.UserComicLibrary;
import com.comic.h.library.enums.ShelfStatus;
import com.comic.h.library.mapper.UserComicLibraryMapper;
import com.comic.h.library.repository.ReadingHistoryRepository;
import com.comic.h.library.repository.UserComicLibraryRepository;
import com.comic.h.library.service.UserComicLibraryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserComicLibraryServiceImpl implements UserComicLibraryService {

    private final UserComicLibraryRepository userComicLibraryRepository;
    private final ReadingHistoryRepository readingHistoryRepository;
    private final ComicService comicService;
    private final UserService userService;
    private final UserComicLibraryMapper userComicLibraryMapper;

    @Override
    @Transactional
    public UserComicLibraryResponse updateLibraryStatus(LibraryStatusRequest request, String username) {
        User user = userService.getUserEntityByUsername(username);
        Comic comic = comicService.getComicEntityById(request.getComicId());

        Optional<UserComicLibrary> existingOpt = userComicLibraryRepository.findByUserUserIdAndComicId(user.getUserId(), comic.getId());

        if (request.getStatus() == null) {
            // Remove from library if status is set to null
            existingOpt.ifPresent(userComicLibraryRepository::delete);
            return null;
        }

        UserComicLibrary library;
        if (existingOpt.isPresent()) {
            library = existingOpt.get();
            library.setStatus(request.getStatus());
        } else {
            library = UserComicLibrary.builder()
                    .user(user)
                    .comic(comic)
                    .status(request.getStatus())
                    .build();
        }

        UserComicLibrary saved = userComicLibraryRepository.save(library);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserComicLibraryResponse> getUserLibrary(String username, ShelfStatus status) {
        User user = userService.getUserEntityByUsername(username);
        List<UserComicLibrary> items = userComicLibraryRepository.findByUserIdAndStatus(user.getUserId(), status);
        if (items.isEmpty()) {
            return List.of();
        }

        // Preload user's reading histories in 1 query to avoid N+1 queries during response mapping
        java.util.Map<Long, ReadingHistory> historyMap = readingHistoryRepository.findAllByUserIdOrderByUpdatedAtDesc(user.getUserId())
                .stream()
                .filter(h -> h.getComic() != null)
                .collect(Collectors.toMap(
                        h -> h.getComic().getId(),
                        h -> h,
                        (h1, h2) -> h1
                ));

        return items.stream()
                .map(item -> userComicLibraryMapper.toResponse(item, historyMap.get(item.getComic().getId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserComicLibraryResponse getComicLibraryStatus(Long comicId, String username) {
        User user = userService.getUserEntityByUsername(username);
        return userComicLibraryRepository.findByUserUserIdAndComicId(user.getUserId(), comicId)
                .map(this::mapToResponse)
                .orElse(null);
    }



    private UserComicLibraryResponse mapToResponse(UserComicLibrary library) {
        // Find reading progress for this comic if available
        Optional<ReadingHistory> historyOpt = readingHistoryRepository.findByUserUserIdAndComicId(
                library.getUser().getUserId(), library.getComic().getId());

        return userComicLibraryMapper.toResponse(library, historyOpt.orElse(null));
    }
}
