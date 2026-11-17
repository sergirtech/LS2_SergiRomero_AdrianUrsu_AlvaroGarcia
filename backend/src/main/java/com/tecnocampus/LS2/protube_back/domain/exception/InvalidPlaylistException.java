package com.tecnocampus.LS2.protube_back.domain.exception;

public class InvalidPlaylistException extends RuntimeException {
    public InvalidPlaylistException(String message) {
        super(message);
    }
}
