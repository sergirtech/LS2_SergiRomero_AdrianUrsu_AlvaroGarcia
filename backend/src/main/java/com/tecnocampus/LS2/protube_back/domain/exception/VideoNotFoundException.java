package com.tecnocampus.LS2.protube_back.domain.exception;

public class VideoNotFoundException extends RuntimeException {
    public VideoNotFoundException(Long id) {
        super("Video " + id + " not found");
    }
}
