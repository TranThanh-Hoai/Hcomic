package com.comic.h.library.service;

import java.util.List;

import com.comic.h.library.dto.request.LibraryStatusRequest;
import com.comic.h.library.dto.response.UserComicLibraryResponse;
import com.comic.h.library.enums.ShelfStatus;

public interface UserComicLibraryService {

    UserComicLibraryResponse updateLibraryStatus(LibraryStatusRequest request, String username);

    List<UserComicLibraryResponse> getUserLibrary(String username, ShelfStatus status);

    UserComicLibraryResponse getComicLibraryStatus(Long comicId, String username);
}
