package com.tecnocampus.LS2.protube_back.application.port.in;

import com.tecnocampus.LS2.protube_back.domain.model.VideoSummary;

public interface GetVideoUseCase {
    VideoSummary getVideo(Long videoId);
}
