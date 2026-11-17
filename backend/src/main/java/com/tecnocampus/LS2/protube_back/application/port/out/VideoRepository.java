package com.tecnocampus.LS2.protube_back.application.port.out;

import com.tecnocampus.LS2.protube_back.domain.model.Video;
import com.tecnocampus.LS2.protube_back.domain.model.VideoFile;
import com.tecnocampus.LS2.protube_back.domain.model.VideoSummary;

import java.util.List;
import java.util.Optional;

/** Output port for persistent video metadata and file content. */
public interface VideoRepository {
    VideoSummary save(Video video);

    List<VideoSummary> findPage(Long cursor, int limit);

    List<VideoSummary> findPageForOwner(Long ownerId, Long cursor, int limit);

    Optional<VideoSummary> findSummaryById(Long id);

    Optional<VideoFile> findFileById(Long id);

    VideoSummary updateMetadata(Long id, String title, String description);

    void deleteById(Long id);
}
