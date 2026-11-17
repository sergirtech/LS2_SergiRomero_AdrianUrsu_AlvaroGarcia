package com.tecnocampus.LS2.protube_back.application.port.in;

import com.tecnocampus.LS2.protube_back.domain.model.VideoFile;

public interface GetVideoFileUseCase {
    VideoFile getVideoFile(Long videoId);
}
