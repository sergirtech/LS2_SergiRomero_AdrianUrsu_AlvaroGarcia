package com.tecnocampus.LS2.protube_back.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static com.tecnocampus.LS2.protube_back.domain.model.VideoFixtures.ownedVideo;
import static com.tecnocampus.LS2.protube_back.domain.model.VideoFixtures.video;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VideoTest {

    @Test
    void importedVideosHaveNoOwnerTagsOrCategory() {
        Video imported = video(1L, "0");

        assertNull(imported.ownerId());
        assertEquals(List.of(), imported.tags());
        assertNull(imported.category());
        assertFalse(imported.isOwnedBy(1L));
    }

    @Test
    void onlyTheUploaderOwnsTheVideo() {
        Video uploaded = ownedVideo(5L, 1L);

        assertTrue(uploaded.isOwnedBy(1L));
        assertFalse(uploaded.isOwnedBy(2L));
        assertFalse(uploaded.isOwnedBy(null));
    }

    @Test
    void nullTagsBecomeAnEmptyList() {
        Video video = new Video(1L, "k", "T", "a", "d", 0, 0, 0, 0, 0, Instant.EPOCH, 1L, null, null);

        assertEquals(List.of(), video.tags());
    }

    @Test
    void withMetadataKeepsEverythingElse() {
        Video original = ownedVideo(5L, 1L);

        Video edited = original.withMetadata("New", "Text", List.of("a"), "Gaming");

        assertEquals("New", edited.title());
        assertEquals("Text", edited.description());
        assertEquals(List.of("a"), edited.tags());
        assertEquals("Gaming", edited.category());
        assertEquals(original.id(), edited.id());
        assertEquals(original.fileKey(), edited.fileKey());
        assertEquals(original.ownerId(), edited.ownerId());
        assertEquals(original.publishedAt(), edited.publishedAt());
    }
}
