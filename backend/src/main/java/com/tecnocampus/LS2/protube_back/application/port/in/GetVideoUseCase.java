package com.tecnocampus.LS2.protube_back.application.port.in;

import com.tecnocampus.LS2.protube_back.domain.model.Video;

/** Input port: one video by id. Throws VideoNotFoundException if it doesn't exist. */
public interface GetVideoUseCase {
    Video getVideo(Long id);
}
