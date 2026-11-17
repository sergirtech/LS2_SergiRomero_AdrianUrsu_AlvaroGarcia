package com.tecnocampus.LS2.protube_back.domain.exception;

/** The video file could not be written to or removed from the store. Mapped to 500. */
public class VideoStorageException extends RuntimeException {
    public VideoStorageException(String message) {
        super(message);
    }

    public VideoStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
