package com.tecnocampus.LS2.protube_back.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tecnocampus.LS2.protube_back.domain.model.Video;

import java.time.Instant;

// ISO-8601 string: @EnableWebMvc (MvcConfig) bypasses Boot's Jackson defaults, which would write a number.
public record VideoDetailResponse(Long id, String fileKey, String title, String author, String description,
                                  double durationSeconds, int width, int height, long viewCount,
                                  long likeCount,
                                  @JsonFormat(shape = JsonFormat.Shape.STRING) Instant publishedAt) {
    public static VideoDetailResponse from(Video video) {
        return new VideoDetailResponse(video.id(), video.fileKey(), video.title(), video.author(),
                video.description(), video.durationSeconds(), video.width(), video.height(),
                video.viewCount(), video.likeCount(), video.publishedAt());
    }
}
