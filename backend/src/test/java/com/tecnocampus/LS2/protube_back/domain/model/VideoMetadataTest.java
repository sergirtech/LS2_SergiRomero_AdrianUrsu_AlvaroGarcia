package com.tecnocampus.LS2.protube_back.domain.model;

import com.tecnocampus.LS2.protube_back.domain.exception.InvalidVideoException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VideoMetadataTest {

    @Test
    void normalizesTheUserInput() {
        VideoMetadata metadata = VideoMetadata.of("  Title ", "  Text ", Arrays.asList("Rock", " rock ", "", null, "Live"),
                "  Music ");

        assertEquals("Title", metadata.title());
        assertEquals("Text", metadata.description());
        assertEquals(List.of("rock", "live"), metadata.tags());
        assertEquals("Music", metadata.category());
    }

    @Test
    void missingOptionalFieldsBecomeEmpty() {
        VideoMetadata metadata = VideoMetadata.of("Title", null, null, "   ");

        assertEquals("", metadata.description());
        assertEquals(List.of(), metadata.tags());
        assertNull(metadata.category());
    }

    @Test
    void titleIsRequiredAndLimited() {
        assertThrows(InvalidVideoException.class, () -> VideoMetadata.of(null, "", List.of(), null));
        assertThrows(InvalidVideoException.class, () -> VideoMetadata.of("   ", "", List.of(), null));
        assertThrows(InvalidVideoException.class,
                () -> VideoMetadata.of("x".repeat(Video.MAX_TITLE_LENGTH + 1), "", List.of(), null));
        assertEquals(Video.MAX_TITLE_LENGTH,
                VideoMetadata.of("x".repeat(Video.MAX_TITLE_LENGTH), "", List.of(), null).title().length());
    }

    @Test
    void descriptionAndCategoryAreLimited() {
        assertThrows(InvalidVideoException.class,
                () -> VideoMetadata.of("Title", "x".repeat(Video.MAX_DESCRIPTION_LENGTH + 1), List.of(), null));
        assertThrows(InvalidVideoException.class,
                () -> VideoMetadata.of("Title", "", List.of(), "x".repeat(Video.MAX_CATEGORY_LENGTH + 1)));
    }

    @Test
    void tagsAreLimitedInNumberAndLength() {
        List<String> tooMany = new ArrayList<>();
        for (int i = 0; i <= Video.MAX_TAGS; i++) {
            tooMany.add("tag" + i);
        }
        assertThrows(InvalidVideoException.class, () -> VideoMetadata.of("Title", "", tooMany, null));
        assertThrows(InvalidVideoException.class,
                () -> VideoMetadata.of("Title", "", List.of("x".repeat(Video.MAX_TAG_LENGTH + 1)), null));

        // Duplicates count once, so ten distinct tags written twice are fine.
        List<String> repeated = new ArrayList<>(tooMany.subList(0, Video.MAX_TAGS));
        repeated.addAll(Collections.nCopies(3, "tag0"));
        assertEquals(Video.MAX_TAGS, VideoMetadata.of("Title", "", repeated, null).tags().size());
    }
}
