package com.tecnocampus.LS2.protube_back.domain.model;

import com.tecnocampus.LS2.protube_back.domain.exception.InvalidCommentException;

import java.time.Instant;

/**
 * Domain comment on a video (US-09): plain Java. {@code authorId} is the user who wrote it, or {@code null} for
 * comments imported from the videoGrabber, whose authors are not ProTube users.
 */
public record Comment(Long id, Long videoId, Long authorId, String authorName, String text, Instant createdAt) {

    public static final int MAX_TEXT_LENGTH = 1000;

    /** New comment by a user, not stored yet. @throws InvalidCommentException if the text is blank or too long. */
    public static Comment write(Long videoId, User author, String text) {
        String cleanText = text == null ? "" : text.trim();
        if (cleanText.isEmpty() || cleanText.length() > MAX_TEXT_LENGTH) {
            throw new InvalidCommentException("Comment must have between 1 and " + MAX_TEXT_LENGTH + " characters");
        }
        return new Comment(null, videoId, author.getId(), author.getUsername(), cleanText, Instant.now());
    }

    /** Comment imported from the videoGrabber, not stored yet. A text that is too long is cut, not rejected. */
    public static Comment imported(Long videoId, String authorName, String text, Instant createdAt) {
        return new Comment(null, videoId, null, authorName, truncate(text.trim()), createdAt);
    }

    private static String truncate(String text) {
        if (text.length() <= MAX_TEXT_LENGTH) {
            return text;
        }
        // Never cut an emoji in half: a lone surrogate is not valid text for PostgreSQL.
        int end = Character.isHighSurrogate(text.charAt(MAX_TEXT_LENGTH - 1)) ? MAX_TEXT_LENGTH - 1 : MAX_TEXT_LENGTH;
        return text.substring(0, end);
    }
}
