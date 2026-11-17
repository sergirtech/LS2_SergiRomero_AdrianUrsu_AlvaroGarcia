package com.tecnocampus.LS2.protube_back.domain.model;

/** Binary video data returned only by the playback use case. */
public record VideoFile(String contentType, byte[] content) {
}
