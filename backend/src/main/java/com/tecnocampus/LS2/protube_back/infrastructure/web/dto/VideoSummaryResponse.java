package com.tecnocampus.LS2.protube_back.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tecnocampus.LS2.protube_back.domain.model.Video;

import java.time.Instant;

// ISO-8601 string: @EnableWebMvc (MvcConfig) bypasses Boot's Jackson defaults, which would write a number.
public record VideoSummaryResponse(Long id, String fileKey, String title, String author,
                                   double durationSeconds, long viewCount,
                                   @JsonFormat(shape = JsonFormat.Shape.STRING) Instant publishedAt) {
    public static VideoSummaryResponse from(Video video) {
        return new VideoSummaryResponse(video.id(), video.fileKey(), video.title(), video.author(),
                video.durationSeconds(), video.viewCount(), video.publishedAt());
    }
}
