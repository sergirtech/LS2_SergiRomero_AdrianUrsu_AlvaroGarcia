package com.tecnocampus.LS2.protube_back.application.port.out;

import java.io.InputStream;

/** Output port: the video store folder where the {@code {fileKey}.mp4} files live (served under /media). */
public interface VideoFileStore {
    /** @throws com.tecnocampus.LS2.protube_back.domain.exception.VideoStorageException if it cannot be written */
    void saveVideo(String fileKey, InputStream content);

    /** Removes every file of the video ({@code .mp4}, {@code .webp}, {@code .json}); missing files are ignored. */
    void deleteVideo(String fileKey);
}
