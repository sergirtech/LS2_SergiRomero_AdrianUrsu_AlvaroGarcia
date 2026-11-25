package com.tecnocampus.LS2.protube_back.infrastructure.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tecnocampus.LS2.protube_back.application.port.out.CommentSource.StoredComment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonCommentSourceTest {

    @TempDir
    Path store;

    private Map<String, List<StoredComment>> readAll(Path dir) {
        return new JsonCommentSource(dir.toString(), new ObjectMapper()).readAll();
    }

    @Test
    void readsTheCommentsOfEachVideo() throws IOException {
        Files.writeString(store.resolve("0.json"), """
                { "title": "A", "meta": { "description": "d", "comments": [
                  { "author": "ana", "text": "First", "timestamp": 100, "like_count": 3 },
                  { "author": "bob", "text": "No date" } ] } }""");
        Files.writeString(store.resolve("0.mp4"), "not json");

        assertEquals(Map.of("0", List.of(
                new StoredComment("ana", "First", Instant.ofEpochSecond(100)),
                new StoredComment("bob", "No date", Instant.EPOCH))), readAll(store));
    }

    @Test
    void skipsCommentsWithoutAuthorOrTextAndInvalidFiles() throws IOException {
        Files.writeString(store.resolve("0.json"), """
                { "meta": { "comments": [ null, { "author": " ", "text": "x" }, { "author": "ana", "text": "" },
                  { "text": "x" }, { "author": "ana" } ] } }""");
        Files.writeString(store.resolve("1.json"), "{ \"title\": \"No meta\" }");
        Files.writeString(store.resolve("2.json"), "{ \"meta\": {} }");
        Files.writeString(store.resolve("3.json"), "{ not json");

        assertEquals(Map.of("0", List.of(), "1", List.of(), "2", List.of(), "3", List.of()), readAll(store));
    }

    @Test
    void aMissingStoreHasNoComments() {
        assertEquals(Map.of(), readAll(store.resolve("missing")));
    }
}
