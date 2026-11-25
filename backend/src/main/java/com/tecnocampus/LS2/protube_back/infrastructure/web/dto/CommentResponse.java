package com.tecnocampus.LS2.protube_back.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tecnocampus.LS2.protube_back.domain.model.Comment;

import java.time.Instant;

/** {@code authorId} is null for comments imported from the videoGrabber. */
// ISO-8601 string: @EnableWebMvc (MvcConfig) bypasses Boot's Jackson defaults, which would write a number.
public record CommentResponse(Long id, Long authorId, String authorName, String text,
                              @JsonFormat(shape = JsonFormat.Shape.STRING) Instant createdAt) {
    public static CommentResponse from(Comment comment) {
        return new CommentResponse(comment.id(), comment.authorId(), comment.authorName(), comment.text(),
                comment.createdAt());
    }
}
