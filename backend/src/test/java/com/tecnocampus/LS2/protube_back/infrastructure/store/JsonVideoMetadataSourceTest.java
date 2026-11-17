package com.tecnocampus.LS2.protube_back.infrastructure.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tecnocampus.LS2.protube_back.domain.model.Video;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonVideoMetadataSourceTest {

    @TempDir
    Path store;

    final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void readsGrabberMetadataInNumericOrder() throws IOException {
        writeVideo("10", "Ten");
        writeVideo("2", "Two");

        List<Video> videos = source(store).readAll();

        assertEquals(List.of("2", "10"), videos.stream().map(Video::fileKey).toList());
        Video two = videos.get(0);
        assertNull(two.id());
        assertEquals("Two", two.title());
        assertEquals("Channel", two.author());
        assertEquals("A description", two.description());
        assertEquals(79.0, two.durationSeconds());
        assertEquals(1920, two.width());
        assertEquals(1234L, two.viewCount());
        assertEquals(56L, two.likeCount());
        assertEquals(Instant.ofEpochSecond(1416405618), two.publishedAt());
    }

    @Test
    void skipsInvalidJsonMissingMp4AndMissingTitle() throws IOException {
        writeVideo("0", "Valid");
        Files.writeString(store.resolve("1.json"), "{ not json");
        Files.createFile(store.resolve("1.mp4"));
        Files.writeString(store.resolve("2.json"), json("No mp4"));
        Files.writeString(store.resolve("3.json"), "{\"duration\": 1}");
        Files.createFile(store.resolve("3.mp4"));

        List<Video> videos = source(store).readAll();

        assertEquals(List.of("0"), videos.stream().map(Video::fileKey).toList());
    }

    @Test
    void usesDefaultsWhenOptionalFieldsAreMissing() throws IOException {
        Files.writeString(store.resolve("0.json"), "{\"title\": \"Only title\"}");
        Files.createFile(store.resolve("0.mp4"));

        Video video = source(store).readAll().get(0);

        assertEquals("Only title", video.title());
        assertNull(video.description());
        assertEquals(0, video.viewCount());
        assertNull(video.publishedAt());
    }

    @Test
    void truncatesVeryLongDescriptions() throws IOException {
        String longText = "x".repeat(Video.MAX_DESCRIPTION_LENGTH + 5);
        Files.writeString(store.resolve("0.json"),
                "{\"title\": \"Long\", \"meta\": {\"description\": \"" + longText + "\"}}");
        Files.createFile(store.resolve("0.mp4"));

        Video video = source(store).readAll().get(0);

        assertEquals(Video.MAX_DESCRIPTION_LENGTH, video.description().length());
    }

    @Test
    void returnsEmptyListWhenStoreDoesNotExist() {
        assertEquals(List.of(), source(store.resolve("missing")).readAll());
    }

    private JsonVideoMetadataSource source(Path dir) {
        return new JsonVideoMetadataSource(dir.toString(), objectMapper);
    }

    private void writeVideo(String fileKey, String title) throws IOException {
        Files.writeString(store.resolve(fileKey + ".json"), json(title));
        Files.createFile(store.resolve(fileKey + ".mp4"));
    }

    private static String json(String title) {
        return """
                {
                  "id": 0, "width": 1920, "height": 1080, "duration": 79.0,
                  "title": "%s", "user": "Channel", "timestamp": 1416405618,
                  "meta": { "description": "A description", "view_count": 1234, "like_count": 56,
                            "tags": ["a"], "comments": [] }
                }
                """.formatted(title);
    }
}
