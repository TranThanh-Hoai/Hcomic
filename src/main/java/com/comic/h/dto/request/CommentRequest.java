package com.comic.h.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequest {

    @NotBlank(message = "Comment content cannot be empty")
    @Size(max = 2000, message = "Comment content must not exceed 2000 characters")
    private String content;

    private Long chapterId;
}
