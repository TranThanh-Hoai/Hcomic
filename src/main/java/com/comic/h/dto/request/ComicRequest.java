package com.comic.h.dto.request;

import com.comic.h.enums.ComicStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComicRequest {

    private String title;
    private String description;
    private String author;
    private ComicStatus status;
}
