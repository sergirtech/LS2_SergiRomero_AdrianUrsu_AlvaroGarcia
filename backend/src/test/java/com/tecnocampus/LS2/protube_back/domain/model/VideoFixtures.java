package com.tecnocampus.LS2.protube_back.domain.model;

import java.time.Instant;

/** Test data shared by the video tests. */
public final class VideoFixtures {
    private VideoFixtures() {
    }

    public static Video video(Long id, String fileKey) {
        return new Video(id, fileKey, "Title " + fileKey, "Author", "Description", 60.0, 1920, 1080,
                10, 1, Instant.EPOCH);
    }
}
