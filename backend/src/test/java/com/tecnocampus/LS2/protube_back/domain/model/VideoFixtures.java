package com.tecnocampus.LS2.protube_back.domain.model;

import java.time.Instant;
import java.util.List;

/** Test data shared by the video tests. */
public final class VideoFixtures {
    private VideoFixtures() {
    }

    /** Imported video: no owner. */
    public static Video video(Long id, String fileKey) {
        return new Video(id, fileKey, "Title " + fileKey, "Author", "Description", 60.0, 1920, 1080,
                10, 1, Instant.EPOCH);
    }

    /** Uploaded video with file key {@code upload-{id}}. */
    public static Video ownedVideo(Long id, Long ownerId) {
        return new Video(id, "upload-" + id, "Upload " + id, "ana", "Description", 0, 0, 0, 0, 0, Instant.EPOCH,
                ownerId, List.of("music"), "Music");
    }
}
