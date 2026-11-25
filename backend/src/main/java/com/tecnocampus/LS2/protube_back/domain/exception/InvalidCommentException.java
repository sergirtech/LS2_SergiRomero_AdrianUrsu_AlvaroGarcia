package com.tecnocampus.LS2.protube_back.domain.exception;

/** Comment that breaks a rule (US-09). Mapped to 400. */
public class InvalidCommentException extends RuntimeException {
    public InvalidCommentException(String message) {
        super(message);
    }
}
