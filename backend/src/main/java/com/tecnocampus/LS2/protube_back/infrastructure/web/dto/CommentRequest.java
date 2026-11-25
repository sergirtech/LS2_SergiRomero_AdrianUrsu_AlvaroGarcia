package com.tecnocampus.LS2.protube_back.infrastructure.web.dto;

/** Body of POST /api/videos/{id}/comments (US-09). The rules are checked in the domain (Comment), which answers 400. */
public record CommentRequest(String text) {
}
