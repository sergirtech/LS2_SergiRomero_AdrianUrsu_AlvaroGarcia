package com.tecnocampus.LS2.protube_back.domain.model;

import java.time.Instant;

/**
 * Domain video: plain Java. {@code fileKey} is the base name of its files in the video store
 * ({@code {fileKey}.mp4}, {@code .webp}, {@code .json}); {@code id} is the database id used in URLs.
 */
public record Video(Long id, String fileKey, String title, String author, String description,
                    double durationSeconds, int width, int height, long viewCount, long likeCount,
                    Instant publishedAt) {

    public static final int MAX_DESCRIPTION_LENGTH = 10_000;
}
