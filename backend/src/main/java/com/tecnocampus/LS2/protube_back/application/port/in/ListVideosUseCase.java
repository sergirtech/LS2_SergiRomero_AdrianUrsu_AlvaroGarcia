package com.tecnocampus.LS2.protube_back.application.port.in;

import com.tecnocampus.LS2.protube_back.domain.model.PageResult;
import com.tecnocampus.LS2.protube_back.domain.model.Video;

/** Input port: paginated video catalog (US-04). */
public interface ListVideosUseCase {
    int DEFAULT_PAGE_SIZE = 12;
    int MAX_PAGE_SIZE = 48;

    PageResult<Video> listVideos(int page, int size);
}
