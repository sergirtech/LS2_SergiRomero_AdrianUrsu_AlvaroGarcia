package com.tecnocampus.LS2.protube_back.domain.model;

import java.util.List;

/** A catalog page; a null cursor means there are no more records. */
public record VideoPage(List<VideoSummary> items, Long nextCursor) {
    public VideoPage {
        items = List.copyOf(items);
    }
}
