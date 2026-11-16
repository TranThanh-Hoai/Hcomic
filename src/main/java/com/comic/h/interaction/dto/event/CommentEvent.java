package com.comic.h.interaction.dto.event;

import com.comic.h.interaction.dto.response.CommentResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentEvent {

    private CommentEventType eventType;
    private Long comicId;
    private Long chapterId;
    private Long commentId;
    private CommentResponse data;
}
