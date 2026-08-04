package com.comic.h.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {

    private Long id;
    private Long comicId;
    private Long chapterId;
    private Long userId;
    private String userDisplayName;
    private String userAvatar;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
