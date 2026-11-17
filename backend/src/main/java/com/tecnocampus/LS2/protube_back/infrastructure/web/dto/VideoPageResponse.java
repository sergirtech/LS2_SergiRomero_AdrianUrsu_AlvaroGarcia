package com.tecnocampus.LS2.protube_back.infrastructure.web.dto;

import com.tecnocampus.LS2.protube_back.domain.model.PageResult;
import com.tecnocampus.LS2.protube_back.domain.model.Video;

import java.util.List;

public record VideoPageResponse(List<VideoSummaryResponse> items, int page, int size, long totalItems,
                                boolean hasNext) {
    public static VideoPageResponse from(PageResult<Video> result) {
        return new VideoPageResponse(result.items().stream().map(VideoSummaryResponse::from).toList(),
                result.page(), result.size(), result.totalItems(), result.hasNext());
    }
}
