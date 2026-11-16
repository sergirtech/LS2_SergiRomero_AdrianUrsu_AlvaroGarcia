package com.tecnocampus.LS2.protube_back.infrastructure.store;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tecnocampus.LS2.protube_back.application.port.out.VideoMetadataSource;
import com.tecnocampus.LS2.protube_back.domain.model.Video;
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
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/** Output adapter: reads the {@code {fileKey}.json} files that tooling/videoGrabber writes in the video store. */
@Component
public class JsonVideoMetadataSource implements VideoMetadataSource {
    private static final Logger LOG = LoggerFactory.getLogger(JsonVideoMetadataSource.class);
    /** "2" before "10": shorter keys first, then alphabetical. */
    private static final Comparator<String> NUMERIC_ORDER =
            Comparator.comparingInt(String::length).thenComparing(Comparator.naturalOrder());

    private final Path storeDir;
    private final ObjectMapper objectMapper;

    public JsonVideoMetadataSource(@Value("${pro_tube.store.dir}") String storeDir, ObjectMapper objectMapper) {
        this.storeDir = Paths.get(storeDir);
        this.objectMapper = objectMapper;
    }

    @Override
    public List<Video> readAll() {
        if (!Files.isDirectory(storeDir)) {
            LOG.warn("Video store {} does not exist: nothing to import", storeDir);
            return List.of();
        }
        try (Stream<Path> files = Files.list(storeDir)) {
            return files
                    .filter(file -> file.getFileName().toString().endsWith(".json"))
                    .map(this::read)
                    .flatMap(Optional::stream)
                    .sorted(Comparator.comparing(Video::fileKey, NUMERIC_ORDER))
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not list the video store " + storeDir, e);
        }
    }

    private Optional<Video> read(Path jsonFile) {
        String fileName = jsonFile.getFileName().toString();
        String fileKey = fileName.substring(0, fileName.length() - ".json".length());
        if (!Files.isRegularFile(storeDir.resolve(fileKey + ".mp4"))) {
            LOG.warn("Skipping {}: {}.mp4 is missing", fileName, fileKey);
            return Optional.empty();
        }
        try {
            GrabberMetadata metadata = objectMapper.readValue(jsonFile.toFile(), GrabberMetadata.class);
            if (metadata.title() == null || metadata.title().isBlank()) {
                LOG.warn("Skipping {}: it has no title", fileName);
                return Optional.empty();
            }
            return Optional.of(metadata.toVideo(fileKey));
        } catch (IOException e) {
            LOG.warn("Skipping {}: invalid metadata ({})", fileName, e.getMessage());
            return Optional.empty();
        }
    }

    private static String truncate(String text) {
        return text != null && text.length() > Video.MAX_DESCRIPTION_LENGTH
                ? text.substring(0, Video.MAX_DESCRIPTION_LENGTH)
                : text;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record GrabberMetadata(String title, String user, Double duration, Integer width, Integer height,
                           Long timestamp, Meta meta) {
        Video toVideo(String fileKey) {
            Meta m = meta != null ? meta : new Meta(null, null, null);
            return new Video(null, fileKey, title, user, truncate(m.description()),
                    duration != null ? duration : 0,
                    width != null ? width : 0,
                    height != null ? height : 0,
                    m.viewCount() != null ? m.viewCount() : 0,
                    m.likeCount() != null ? m.likeCount() : 0,
                    timestamp != null ? Instant.ofEpochSecond(timestamp) : null);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Meta(String description,
                @JsonProperty("view_count") Long viewCount,
                @JsonProperty("like_count") Long likeCount) {
    }
}
