package com.tecnocampus.LS2.protube_back.domain.model;

import com.tecnocampus.LS2.protube_back.domain.exception.InvalidVideoException;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * The metadata a user can set on their own video (US-06, US-07), already normalized: title and description
 * trimmed, tags trimmed, lowercased and without duplicates or blanks, blank category stored as {@code null}.
 */
public record VideoMetadata(String title, String description, List<String> tags, String category) {

    public VideoMetadata {
        tags = List.copyOf(tags);
    }

    /** Normalizes and validates the user input. @throws InvalidVideoException if a rule is broken. */
    public static VideoMetadata of(String title, String description, List<String> tags, String category) {
        String cleanTitle = title == null ? "" : title.trim();
        if (cleanTitle.isEmpty() || cleanTitle.length() > Video.MAX_TITLE_LENGTH) {
            throw new InvalidVideoException("Title must have between 1 and " + Video.MAX_TITLE_LENGTH + " characters");
        }

        String cleanDescription = description == null ? "" : description.trim();
        if (cleanDescription.length() > Video.MAX_DESCRIPTION_LENGTH) {
            throw new InvalidVideoException(
                    "Description must have at most " + Video.MAX_DESCRIPTION_LENGTH + " characters");
        }

        String cleanCategory = category == null || category.isBlank() ? null : category.trim();
        if (cleanCategory != null && cleanCategory.length() > Video.MAX_CATEGORY_LENGTH) {
            throw new InvalidVideoException(
                    "Category must have at most " + Video.MAX_CATEGORY_LENGTH + " characters");
        }

        return new VideoMetadata(cleanTitle, cleanDescription, normalizeTags(tags), cleanCategory);
    }

    private static List<String> normalizeTags(List<String> tags) {
        Set<String> unique = new LinkedHashSet<>();
        for (String tag : tags == null ? List.<String>of() : tags) {
            String clean = tag == null ? "" : tag.trim().toLowerCase(Locale.ROOT);
            if (clean.isEmpty()) {
                continue;
            }
            if (clean.length() > Video.MAX_TAG_LENGTH) {
                throw new InvalidVideoException("Each tag must have at most " + Video.MAX_TAG_LENGTH + " characters");
            }
            unique.add(clean);
        }
        if (unique.size() > Video.MAX_TAGS) {
            throw new InvalidVideoException("A video can have at most " + Video.MAX_TAGS + " tags");
        }
        return new ArrayList<>(unique);
    }
}
