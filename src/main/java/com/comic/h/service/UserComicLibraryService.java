package com.comic.h.service;

import java.util.List;
import com.comic.h.dto.request.LibraryStatusRequest;
import com.comic.h.dto.response.UserComicLibraryResponse;
import com.comic.h.enums.ShelfStatus;

public interface UserComicLibraryService {

    UserComicLibraryResponse updateLibraryStatus(LibraryStatusRequest request, String username);

    List<UserComicLibraryResponse> getUserLibrary(String username, ShelfStatus status);

    UserComicLibraryResponse getComicLibraryStatus(Long comicId, String username);
}
