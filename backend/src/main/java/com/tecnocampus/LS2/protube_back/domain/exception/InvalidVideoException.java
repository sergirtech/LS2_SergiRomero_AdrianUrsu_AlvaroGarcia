package com.tecnocampus.LS2.protube_back.domain.exception;

/** Upload or metadata that breaks a video rule (US-06, US-07). Mapped to 400. */
public class InvalidVideoException extends RuntimeException {
    public InvalidVideoException(String message) {
        super(message);
    }
}
