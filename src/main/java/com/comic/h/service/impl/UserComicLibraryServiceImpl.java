package com.comic.h.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comic.h.dto.request.LibraryStatusRequest;
import com.comic.h.dto.response.UserComicLibraryResponse;
import com.comic.h.entity.Comic;
import com.comic.h.entity.ReadingHistory;
import com.comic.h.entity.User;
import com.comic.h.entity.UserComicLibrary;
import com.comic.h.enums.ShelfStatus;
import com.comic.h.exception.ResourceNotFoundException;
import com.comic.h.mapper.UserComicLibraryMapper;
import com.comic.h.repository.ComicRepository;
import com.comic.h.repository.ReadingHistoryRepository;
import com.comic.h.repository.UserComicLibraryRepository;
import com.comic.h.repository.UserRepository;
import com.comic.h.service.UserComicLibraryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserComicLibraryServiceImpl implements UserComicLibraryService {

    private final UserComicLibraryRepository userComicLibraryRepository;
    private final ReadingHistoryRepository readingHistoryRepository;
    private final ComicRepository comicRepository;
    private final UserRepository userRepository;
    private final UserComicLibraryMapper userComicLibraryMapper;

    @Override
    @Transactional
    public UserComicLibraryResponse updateLibraryStatus(LibraryStatusRequest request, String username) {
        User user = findUserByUsername(username);
        Comic comic = comicRepository.findById(request.getComicId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy truyện với id: " + request.getComicId()));

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
        User user = findUserByUsername(username);
        List<UserComicLibrary> items = userComicLibraryRepository.findByUserIdAndStatus(user.getUserId(), status);
        return items.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserComicLibraryResponse getComicLibraryStatus(Long comicId, String username) {
        User user = findUserByUsername(username);
        return userComicLibraryRepository.findByUserUserIdAndComicId(user.getUserId(), comicId)
                .map(this::mapToResponse)
                .orElse(null);
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng: " + username));
    }

    private UserComicLibraryResponse mapToResponse(UserComicLibrary library) {
        // Find reading progress for this comic if available
        Optional<ReadingHistory> historyOpt = readingHistoryRepository.findByUserUserIdAndComicId(
                library.getUser().getUserId(), library.getComic().getId());

        return userComicLibraryMapper.toResponse(library, historyOpt.orElse(null));
    }
}
