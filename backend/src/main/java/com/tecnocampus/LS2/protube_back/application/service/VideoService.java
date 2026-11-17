package com.tecnocampus.LS2.protube_back.application.service;

import com.tecnocampus.LS2.protube_back.application.port.in.GetVideoUseCase;
import com.tecnocampus.LS2.protube_back.application.port.in.ImportVideosUseCase;
import com.tecnocampus.LS2.protube_back.application.port.in.ListVideosUseCase;
import com.tecnocampus.LS2.protube_back.application.port.out.VideoMetadataSource;
import com.tecnocampus.LS2.protube_back.application.port.out.VideoRepository;
import com.tecnocampus.LS2.protube_back.domain.exception.VideoNotFoundException;
import com.tecnocampus.LS2.protube_back.domain.model.PageResult;
import com.tecnocampus.LS2.protube_back.domain.model.Video;
import org.springframework.stereotype.Service;

@Service
public class VideoService implements ListVideosUseCase, GetVideoUseCase, ImportVideosUseCase {

    private final VideoRepository videoRepository;
    private final VideoMetadataSource metadataSource;

    public VideoService(VideoRepository videoRepository, VideoMetadataSource metadataSource) {
        this.videoRepository = videoRepository;
        this.metadataSource = metadataSource;
    }

    @Override
    public PageResult<Video> listVideos(int page, int size) {
        // Out-of-range values are clamped instead of rejected, so the list never errors.
        return videoRepository.findPage(Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE));
    }

    @Override
    public Video getVideo(Long id) {
        return videoRepository.findById(id).orElseThrow(() -> new VideoNotFoundException(id));
    }

    @Override
    public int importFromStore() {
        int imported = 0;
        for (Video video : metadataSource.readAll()) {
            if (!videoRepository.existsByFileKey(video.fileKey())) {
                videoRepository.save(video);
                imported++;
            }
        }
        return imported;
    }
}
