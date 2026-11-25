package com.tecnocampus.LS2.protube_back.infrastructure.store;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tecnocampus.LS2.protube_back.application.port.out.CommentSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/** Output adapter: reads the comments of the {@code {fileKey}.json} files that tooling/videoGrabber writes. */
@Component
public class JsonCommentSource implements CommentSource {
    private static final Logger LOG = LoggerFactory.getLogger(JsonCommentSource.class);
    private static final String EXTENSION = ".json";

    private final Path storeDir;
    private final ObjectMapper objectMapper;

    public JsonCommentSource(@Value("${pro_tube.store.dir}") String storeDir, ObjectMapper objectMapper) {
        this.storeDir = Paths.get(storeDir);
        this.objectMapper = objectMapper;
    }

    @Override
    public Map<String, List<StoredComment>> readAll() {
        if (!Files.isDirectory(storeDir)) {
            return Map.of();
        }
        Map<String, List<StoredComment>> commentsByFileKey = new HashMap<>();
        try (Stream<Path> files = Files.list(storeDir)) {
            files.forEach(file -> {
                String fileName = file.getFileName().toString();
                if (fileName.endsWith(EXTENSION)) {
                    commentsByFileKey.put(fileName.substring(0, fileName.length() - EXTENSION.length()), read(file));
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException("Could not list the video store " + storeDir, e);
        }
        return commentsByFileKey;
    }

    private List<StoredComment> read(Path jsonFile) {
        try {
            GrabberFile file = objectMapper.readValue(jsonFile.toFile(), GrabberFile.class);
            if (file.meta() == null || file.meta().comments() == null) {
                return List.of();
            }
            return file.meta().comments().stream()
                    .filter(comment -> comment != null && hasText(comment.author()) && hasText(comment.text()))
                    .map(GrabberComment::toStoredComment)
                    .toList();
        } catch (IOException e) {
            LOG.warn("Skipping the comments of {}: invalid metadata ({})", jsonFile.getFileName(), e.getMessage());
            return List.of();
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record GrabberFile(Meta meta) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Meta(List<GrabberComment> comments) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record GrabberComment(String author, String text, Long timestamp) {
        StoredComment toStoredComment() {
            // Without a date the comment goes to the end of the list (newest first).
            return new StoredComment(author, text,
                    timestamp != null ? Instant.ofEpochSecond(timestamp) : Instant.EPOCH);
        }
    }
}
