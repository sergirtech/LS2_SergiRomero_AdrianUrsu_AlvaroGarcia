package com.tecnocampus.LS2.protube_back.domain.exception;

public class CommentNotFoundException extends RuntimeException {
    public CommentNotFoundException(Long id) {
        super("Comment not found: " + id);
    }
}
