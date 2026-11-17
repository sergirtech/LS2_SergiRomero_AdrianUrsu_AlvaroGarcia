package com.tecnocampus.LS2.protube_back.application.port.in;

import com.tecnocampus.LS2.protube_back.domain.model.VideoPage;

/** Input port for cursor-based video catalog browsing. */
public interface ListVideosUseCase {
    VideoPage getVideos(Long cursor, int limit);
}
