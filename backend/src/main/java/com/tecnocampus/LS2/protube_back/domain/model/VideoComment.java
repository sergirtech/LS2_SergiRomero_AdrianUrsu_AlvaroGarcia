package com.tecnocampus.LS2.protube_back.domain.model;

import java.time.Instant;

public record VideoComment(Long id, Long videoId, String videoTitle, Long authorId, String authorUsername,
                           Long parentId, String text, Instant createdAt, long likeCount,
                           boolean likedByCurrentUser) {
}
