package com.comic.h.library.dto.request;

import com.comic.h.library.enums.ShelfStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LibraryStatusRequest {

    @NotNull(message = "comicId must not be null")
    private Long comicId;

    // Status: READING, FAVORITE, COMPLETED, READ_LATER. If null, removes from library.
    private ShelfStatus status;
}
