package com.tecnocampus.LS2.protube_back.domain.exception;

public class PlaylistNotFoundException extends RuntimeException {
    public PlaylistNotFoundException(Long id) {
        super("Playlist not found: " + id);
    }
}
