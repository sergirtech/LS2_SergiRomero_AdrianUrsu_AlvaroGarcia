package com.tecnocampus.LS2.protube_back.domain.exception;

/** Also used when the current user does not own a requested video. */
public class VideoNotFoundException extends RuntimeException {
    public VideoNotFoundException(Long id) {
        super("Video " + id + " was not found");
    }
}
