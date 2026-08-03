package com.comic.h.dto.request;

import com.comic.h.enums.ComicStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

    @NotBlank(message = "Comic title cannot be empty")
    @Size(max = 255, message = "Comic title must not exceed 255 characters")
    private String title;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @Size(max = 255, message = "Author must not exceed 255 characters")
    private String author;

    private ComicStatus status;
}
