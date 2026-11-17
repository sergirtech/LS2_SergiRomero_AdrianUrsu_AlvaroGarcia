package com.tecnocampus.LS2.protube_back.infrastructure.web.dto;

import com.tecnocampus.LS2.protube_back.domain.model.VideoPage;

import java.util.List;

public record VideoPageResponse(List<VideoResponse> items, Long nextCursor) {
    public static VideoPageResponse from(VideoPage page) {
        return new VideoPageResponse(page.items().stream().map(VideoResponse::from).toList(), page.nextCursor());
    }
}
