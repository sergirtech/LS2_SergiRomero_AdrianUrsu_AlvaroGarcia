package com.tecnocampus.LS2.protube_back.infrastructure.web.dto;

import com.tecnocampus.LS2.protube_back.domain.model.VideoComment;

import java.time.Instant;

public record CommentResponse(Long id, Long videoId, String videoTitle, Long authorId, String authorUsername,
                              Long parentId, String text, Instant createdAt, long likeCount,
                              boolean likedByCurrentUser) {
    public static CommentResponse from(VideoComment comment) {
        return new CommentResponse(comment.id(), comment.videoId(), comment.videoTitle(), comment.authorId(),
                comment.authorUsername(),
                comment.parentId(), comment.text(), comment.createdAt(), comment.likeCount(),
                comment.likedByCurrentUser());
    }
}
