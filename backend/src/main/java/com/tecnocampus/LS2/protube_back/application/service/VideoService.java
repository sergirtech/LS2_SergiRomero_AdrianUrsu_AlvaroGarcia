package com.tecnocampus.LS2.protube_back.application.service;

import com.tecnocampus.LS2.protube_back.application.port.in.DeleteVideoUseCase;
import com.tecnocampus.LS2.protube_back.application.port.in.EditVideoUseCase;
import com.tecnocampus.LS2.protube_back.application.port.in.FindUserUseCase;
import com.tecnocampus.LS2.protube_back.application.port.in.GetVideoFileUseCase;
import com.tecnocampus.LS2.protube_back.application.port.in.GetVideoUseCase;
import com.tecnocampus.LS2.protube_back.application.port.in.ListVideosUseCase;
import com.tecnocampus.LS2.protube_back.application.port.in.ListMyVideosUseCase;
import com.tecnocampus.LS2.protube_back.application.port.in.UploadVideoUseCase;
import com.tecnocampus.LS2.protube_back.application.port.out.VideoRepository;
import com.tecnocampus.LS2.protube_back.domain.exception.InvalidVideoException;
import com.tecnocampus.LS2.protube_back.domain.exception.VideoNotFoundException;
import com.tecnocampus.LS2.protube_back.domain.model.User;
import com.tecnocampus.LS2.protube_back.domain.model.Video;
import com.tecnocampus.LS2.protube_back.domain.model.VideoFile;
import com.tecnocampus.LS2.protube_back.domain.model.VideoPage;
import com.tecnocampus.LS2.protube_back.domain.model.VideoSummary;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Service
public class VideoService implements ListVideosUseCase, UploadVideoUseCase, EditVideoUseCase,
        DeleteVideoUseCase, GetVideoUseCase, GetVideoFileUseCase, ListMyVideosUseCase {
    private static final int MAX_PAGE_SIZE = 50;
    private static final int MAX_TITLE_LENGTH = 150;
    private static final int MAX_DESCRIPTION_LENGTH = 2_000;
    private static final long MAX_FILE_SIZE = 100L * 1024 * 1024;
    private static final String MP4_CONTENT_TYPE = "video/mp4";

    private final VideoRepository videoRepository;
    private final FindUserUseCase findUserUseCase;

    public VideoService(VideoRepository videoRepository, FindUserUseCase findUserUseCase) {
        this.videoRepository = videoRepository;
        this.findUserUseCase = findUserUseCase;
    }

    @Override
    public VideoPage getVideos(Long cursor, int limit) {
        validatePageRequest(cursor, limit);
        return createPage(videoRepository.findPage(cursor, limit + 1), limit);
    }

    @Override
    public VideoPage getMyVideos(String ownerEmail, Long cursor, int limit) {
        validatePageRequest(cursor, limit);
        User owner = findUserUseCase.getByEmail(ownerEmail);
        return createPage(videoRepository.findPageForOwner(owner.getId(), cursor, limit + 1), limit);
    }

    private static void validatePageRequest(Long cursor, int limit) {
        if (limit < 1 || limit > MAX_PAGE_SIZE) {
            throw new InvalidVideoException("Page size must be between 1 and " + MAX_PAGE_SIZE);
        }
        if (cursor != null && cursor < 1) {
            throw new InvalidVideoException("Cursor must be a positive video id");
        }
    }

    private static VideoPage createPage(List<VideoSummary> results, int limit) {
        Long nextCursor = results.size() > limit ? results.get(limit - 1).id() : null;
        List<VideoSummary> items = results.size() > limit ? results.subList(0, limit) : results;
        return new VideoPage(items, nextCursor);
    }

    @Override
    public VideoSummary upload(String ownerEmail, String title, String description, String fileName, byte[] content) {
        String normalizedTitle = validateTitle(title);
        String normalizedDescription = validateDescription(description, true);
        validateFile(fileName, content);
        User owner = findUserUseCase.getByEmail(ownerEmail);

        Video video = new Video(null, normalizedTitle, normalizedDescription, owner.getId(), owner.getUsername(),
                MP4_CONTENT_TYPE, content, Instant.now());
        return videoRepository.save(video);
    }

    @Override
    public VideoSummary edit(String ownerEmail, Long videoId, String title, String description) {
        User owner = findUserUseCase.getByEmail(ownerEmail);
        VideoSummary video = getOwnedVideo(videoId, owner.getId());
        return videoRepository.updateMetadata(video.id(), validateTitle(title), validateDescription(description, false));
    }

    @Override
    public void delete(String ownerEmail, Long videoId) {
        User owner = findUserUseCase.getByEmail(ownerEmail);
        VideoSummary video = getOwnedVideo(videoId, owner.getId());
        videoRepository.deleteById(video.id());
    }

    @Override
    public VideoSummary getVideo(Long videoId) {
        return videoRepository.findSummaryById(videoId).orElseThrow(() -> new VideoNotFoundException(videoId));
    }

    @Override
    public VideoFile getVideoFile(Long videoId) {
        return videoRepository.findFileById(videoId).orElseThrow(() -> new VideoNotFoundException(videoId));
    }

    private VideoSummary getOwnedVideo(Long videoId, Long ownerId) {
        VideoSummary video = getVideo(videoId);
        if (!video.ownerId().equals(ownerId)) {
            throw new VideoNotFoundException(videoId);
        }
        return video;
    }

    private static String validateTitle(String title) {
        if (title == null || title.isBlank() || title.trim().length() > MAX_TITLE_LENGTH) {
            throw new InvalidVideoException("Title must contain 1 to " + MAX_TITLE_LENGTH + " characters");
        }
        return title.trim();
    }

    private static String validateDescription(String description, boolean required) {
        if (description == null || description.length() > MAX_DESCRIPTION_LENGTH
                || (required && description.isBlank())) {
            throw new InvalidVideoException("Description must be at most " + MAX_DESCRIPTION_LENGTH
                    + " characters" + (required ? " and cannot be empty" : ""));
        }
        return description.trim();
    }

    private static void validateFile(String fileName, byte[] content) {
        if (fileName == null || !fileName.toLowerCase(Locale.ROOT).endsWith(".mp4")) {
            throw new InvalidVideoException("Only .mp4 files are supported");
        }
        if (content == null || content.length == 0) {
            throw new InvalidVideoException("Video file cannot be empty");
        }
        if (content.length > MAX_FILE_SIZE) {
            throw new InvalidVideoException("Video file must be 100 MB or smaller");
        }
        if (content.length < 8 || content[4] != 'f' || content[5] != 't'
                || content[6] != 'y' || content[7] != 'p') {
            throw new InvalidVideoException("File content is not a valid MP4");
        }
    }
}
