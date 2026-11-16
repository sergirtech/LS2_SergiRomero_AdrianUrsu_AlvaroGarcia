package com.tecnocampus.LS2.protube_back.application.port.out;

import com.tecnocampus.LS2.protube_back.domain.model.PageResult;
import com.tecnocampus.LS2.protube_back.domain.model.Video;

import java.util.Optional;

/** Output port: video persistence. */
public interface VideoRepository {
    Video save(Video video);

    Optional<Video> findById(Long id);

    PageResult<Video> findPage(int page, int size);

    boolean existsByFileKey(String fileKey);
}
