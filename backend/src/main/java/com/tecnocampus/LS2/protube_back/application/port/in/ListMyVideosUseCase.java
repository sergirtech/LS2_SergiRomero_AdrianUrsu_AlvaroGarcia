package com.tecnocampus.LS2.protube_back.application.port.in;

import com.tecnocampus.LS2.protube_back.domain.model.PageResult;
import com.tecnocampus.LS2.protube_back.domain.model.Video;

/** Input port: the videos the authenticated user uploaded, newest first (US-07). */
public interface ListMyVideosUseCase {
    PageResult<Video> listMyVideos(String ownerEmail, int page, int size);
}
