package com.tecnocampus.LS2.protube_back.domain.model;

import java.time.Instant;
import java.util.List;

/**
 * Domain video: plain Java. {@code fileKey} is the base name of its files in the video store
 * ({@code {fileKey}.mp4}, {@code .webp}, {@code .json}); {@code id} is the database id used in URLs.
 * {@code ownerId} is the user who uploaded it, or {@code null} for videos imported from the videoGrabber,
 * which nobody can edit or delete (US-07).
 */
public record Video(Long id, String fileKey, String title, String author, String description,
                    double durationSeconds, int width, int height, long viewCount, long likeCount,
                    Instant publishedAt, Long ownerId, List<String> tags, String category) {

    public static final int MAX_TITLE_LENGTH = 150;
    public static final int MAX_DESCRIPTION_LENGTH = 10_000;
    public static final int MAX_TAGS = 10;
    public static final int MAX_TAG_LENGTH = 30;
    public static final int MAX_CATEGORY_LENGTH = 50;

    public Video {
        tags = tags == null ? List.of() : List.copyOf(tags);
    }

    /** Imported video (videoGrabber): no owner, tags or category. */
    public Video(Long id, String fileKey, String title, String author, String description,
                 double durationSeconds, int width, int height, long viewCount, long likeCount,
                 Instant publishedAt) {
        this(id, fileKey, title, author, description, durationSeconds, width, height, viewCount, likeCount,
                publishedAt, null, List.of(), null);
    }

    public boolean isOwnedBy(Long userId) {
        return ownerId != null && ownerId.equals(userId);
    }

    /** Same video with new editable metadata (US-07). */
    public Video withMetadata(String newTitle, String newDescription, List<String> newTags, String newCategory) {
        return new Video(id, fileKey, newTitle, author, newDescription, durationSeconds, width, height, viewCount,
                likeCount, publishedAt, ownerId, newTags, newCategory);
    }
}
