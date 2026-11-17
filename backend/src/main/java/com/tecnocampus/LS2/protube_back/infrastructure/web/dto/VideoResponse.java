package com.tecnocampus.LS2.protube_back.infrastructure.web.dto;

import com.tecnocampus.LS2.protube_back.domain.model.VideoSummary;

import java.time.Instant;

/** Public metadata only; never includes the binary file. */
public record VideoResponse(Long id, String title, String description, Long ownerId, String ownerUsername,
                            Instant createdAt) {
    public static VideoResponse from(VideoSummary video) {
        return new VideoResponse(video.id(), video.title(), video.description(), video.ownerId(),
                video.ownerUsername(), video.createdAt());
    }
}
