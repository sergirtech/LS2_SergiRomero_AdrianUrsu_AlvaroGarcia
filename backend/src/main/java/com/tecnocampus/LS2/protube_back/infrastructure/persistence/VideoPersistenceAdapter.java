package com.tecnocampus.LS2.protube_back.infrastructure.persistence;

import com.tecnocampus.LS2.protube_back.application.port.out.VideoRepository;
import com.tecnocampus.LS2.protube_back.application.port.out.CommentRepository;
import com.tecnocampus.LS2.protube_back.application.port.out.PlaylistRepository;
import com.tecnocampus.LS2.protube_back.domain.exception.UserNotFoundException;
import com.tecnocampus.LS2.protube_back.domain.exception.VideoNotFoundException;
import com.tecnocampus.LS2.protube_back.domain.model.Video;
import com.tecnocampus.LS2.protube_back.domain.model.VideoFile;
import com.tecnocampus.LS2.protube_back.domain.model.VideoSummary;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/** Output adapter that stores video metadata and bytes in related PostgreSQL-compatible tables. */
@Component
public class VideoPersistenceAdapter implements VideoRepository {
    private final VideoJpaRepository videoRepository;
    private final VideoContentJpaRepository contentRepository;
    private final UserJpaRepository userRepository;
    private final CommentRepository commentRepository;
    private final PlaylistRepository playlistRepository;

    VideoPersistenceAdapter(VideoJpaRepository videoRepository,
                            VideoContentJpaRepository contentRepository,
                            UserJpaRepository userRepository,
                            CommentRepository commentRepository,
                            PlaylistRepository playlistRepository) {
        this.videoRepository = videoRepository;
        this.contentRepository = contentRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.playlistRepository = playlistRepository;
    }

    @Override
    @Transactional
    public VideoSummary save(Video video) {
        UserJpaEntity owner = userRepository.findById(video.getOwnerId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        VideoJpaEntity saved = videoRepository.save(VideoJpaEntity.fromDomain(video, owner));
        contentRepository.save(VideoContentJpaEntity.create(saved, video.getContent()));
        return saved.toSummary();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoSummary> findPage(Long cursor, int limit) {
        List<VideoJpaEntity> videos = cursor == null
                ? videoRepository.findAllByOrderByIdDesc(PageRequest.of(0, limit))
                : videoRepository.findByIdLessThanOrderByIdDesc(cursor, PageRequest.of(0, limit));
        return videos.stream().map(VideoJpaEntity::toSummary).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoSummary> findPageForOwner(Long ownerId, Long cursor, int limit) {
        List<VideoJpaEntity> videos = cursor == null
                ? videoRepository.findAllByOwner_IdOrderByIdDesc(ownerId, PageRequest.of(0, limit))
                : videoRepository.findAllByOwner_IdAndIdLessThanOrderByIdDesc(ownerId, cursor,
                        PageRequest.of(0, limit));
        return videos.stream().map(VideoJpaEntity::toSummary).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VideoSummary> findSummaryById(Long id) {
        return videoRepository.findWithOwnerById(id).map(VideoJpaEntity::toSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VideoFile> findFileById(Long id) {
        return contentRepository.findByVideoId(id)
                .map(content -> new VideoFile(content.getVideo().getContentType(), content.getContent()));
    }

    @Override
    @Transactional
    public VideoSummary updateMetadata(Long id, String title, String description) {
        VideoJpaEntity video = videoRepository.findWithOwnerById(id)
                .orElseThrow(() -> new VideoNotFoundException(id));
        video.updateMetadata(title, description);
        return videoRepository.save(video).toSummary();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        playlistRepository.removeVideoFromAll(id);
        commentRepository.deleteByVideoId(id);
        contentRepository.deleteByVideoId(id);
        videoRepository.deleteById(id);
    }
}
