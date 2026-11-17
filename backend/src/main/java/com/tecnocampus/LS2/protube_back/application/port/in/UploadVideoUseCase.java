package com.tecnocampus.LS2.protube_back.application.port.in;

import com.tecnocampus.LS2.protube_back.domain.model.Video;

import java.io.InputStream;
import java.util.List;

/** Input port: an authenticated user uploads an MP4 with its metadata (US-06). */
public interface UploadVideoUseCase {
    long MAX_FILE_SIZE_BYTES = 100L * 1024 * 1024;

    /**
     * @throws com.tecnocampus.LS2.protube_back.domain.exception.InvalidVideoException if the file or metadata is
     *         not valid; nothing is stored then
     */
    Video upload(String ownerEmail, UploadVideoCommand command);

    /** What the user sends: the metadata and the file, read once as a stream. */
    record UploadVideoCommand(String title, String description, List<String> tags, String category,
                              String fileName, long sizeBytes, InputStream content) {
    }
}
