package com.tecnocampus.LS2.protube_back.domain.exception;

/** A user tried to change a video they don't own (US-07). Mapped to 403. */
public class VideoAccessDeniedException extends RuntimeException {
    public VideoAccessDeniedException(Long videoId) {
        super("You can only change your own videos (video " + videoId + ")");
    }
}
