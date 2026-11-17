package com.tecnocampus.LS2.protube_back.application.service;

import com.tecnocampus.LS2.protube_back.application.port.in.DeleteVideoUseCase;
import com.tecnocampus.LS2.protube_back.application.port.in.EditVideoUseCase;
import com.tecnocampus.LS2.protube_back.application.port.in.GetVideoUseCase;
import com.tecnocampus.LS2.protube_back.application.port.in.ImportVideosUseCase;
import com.tecnocampus.LS2.protube_back.application.port.in.ListMyVideosUseCase;
import com.tecnocampus.LS2.protube_back.application.port.in.ListVideosUseCase;
import com.tecnocampus.LS2.protube_back.application.port.in.UploadVideoUseCase;
import com.tecnocampus.LS2.protube_back.application.port.out.UserRepository;
import com.tecnocampus.LS2.protube_back.application.port.out.VideoFileStore;
import com.tecnocampus.LS2.protube_back.application.port.out.VideoMetadataSource;
import com.tecnocampus.LS2.protube_back.application.port.out.VideoRepository;
import com.tecnocampus.LS2.protube_back.domain.exception.InvalidVideoException;
import com.tecnocampus.LS2.protube_back.domain.exception.UserNotFoundException;
import com.tecnocampus.LS2.protube_back.domain.exception.VideoAccessDeniedException;
import com.tecnocampus.LS2.protube_back.domain.exception.VideoNotFoundException;
import com.tecnocampus.LS2.protube_back.domain.exception.VideoStorageException;
import com.tecnocampus.LS2.protube_back.domain.model.PageResult;
import com.tecnocampus.LS2.protube_back.domain.model.User;
import com.tecnocampus.LS2.protube_back.domain.model.Video;
import com.tecnocampus.LS2.protube_back.domain.model.VideoMetadata;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class VideoService implements ListVideosUseCase, GetVideoUseCase, ImportVideosUseCase, UploadVideoUseCase,
        EditVideoUseCase, DeleteVideoUseCase, ListMyVideosUseCase {

    /** Bytes 4-7 of every MP4 (ISO base media file) are the "ftyp" box type. */
    private static final byte[] MP4_SIGNATURE = "ftyp".getBytes(StandardCharsets.US_ASCII);
    private static final int MP4_HEADER_LENGTH = 8;

    private final VideoRepository videoRepository;
    private final VideoMetadataSource metadataSource;
    private final VideoFileStore fileStore;
    private final UserRepository userRepository;

    public VideoService(VideoRepository videoRepository, VideoMetadataSource metadataSource,
                        VideoFileStore fileStore, UserRepository userRepository) {
        this.videoRepository = videoRepository;
        this.metadataSource = metadataSource;
        this.fileStore = fileStore;
        this.userRepository = userRepository;
    }

    @Override
    public PageResult<Video> listVideos(int page, int size) {
        // Out-of-range values are clamped instead of rejected, so the list never errors.
        return videoRepository.findPage(Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE));
    }

    @Override
    public PageResult<Video> listMyVideos(String ownerEmail, int page, int size) {
        User owner = findUser(ownerEmail);
        return videoRepository.findPageByOwner(owner.getId(), Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE));
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

    @Override
    public Video upload(String ownerEmail, UploadVideoCommand command) {
        VideoMetadata metadata = VideoMetadata.of(command.title(), command.description(), command.tags(),
                command.category());
        validateFile(command.fileName(), command.sizeBytes());
        User owner = findUser(ownerEmail);
        InputStream content = checkedMp4(command.content());

        // A fresh key per upload never clashes with the videoGrabber files ("0", "1", ...).
        String fileKey = "upload-" + UUID.randomUUID();
        fileStore.saveVideo(fileKey, content);
        try {
            return videoRepository.save(new Video(null, fileKey, metadata.title(), owner.getUsername(),
                    metadata.description(), 0, 0, 0, 0, 0, Instant.now(), owner.getId(), metadata.tags(),
                    metadata.category()));
        } catch (RuntimeException e) {
            // No video row means nobody can reach the file: remove it so no orphan is left (US-06).
            fileStore.deleteVideo(fileKey);
            throw e;
        }
    }

    @Override
    public Video edit(String ownerEmail, Long videoId, String title, String description, List<String> tags,
                      String category) {
        Video video = getOwnedVideo(ownerEmail, videoId);
        VideoMetadata metadata = VideoMetadata.of(title, description, tags, category);
        return videoRepository.save(video.withMetadata(metadata.title(), metadata.description(), metadata.tags(),
                metadata.category()));
    }

    @Override
    public void delete(String ownerEmail, Long videoId) {
        Video video = getOwnedVideo(ownerEmail, videoId);
        videoRepository.deleteById(video.id());
        fileStore.deleteVideo(video.fileKey());
    }

    private Video getOwnedVideo(String ownerEmail, Long videoId) {
        User user = findUser(ownerEmail);
        Video video = getVideo(videoId);
        if (!video.isOwnedBy(user.getId())) {
            throw new VideoAccessDeniedException(videoId);
        }
        return video;
    }

    private User findUser(String email) {
        return userRepository.findByEmail(User.normalizeEmail(email))
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    private static void validateFile(String fileName, long sizeBytes) {
        if (fileName == null || !fileName.toLowerCase(Locale.ROOT).endsWith(".mp4")) {
            throw new InvalidVideoException("Only .mp4 files are supported");
        }
        if (sizeBytes <= 0) {
            throw new InvalidVideoException("The video file is empty");
        }
        if (sizeBytes > MAX_FILE_SIZE_BYTES) {
            throw new InvalidVideoException("The video file must be 100 MB or smaller");
        }
    }

    /** Checks the MP4 signature without consuming it: the returned stream still starts at byte 0. */
    private static InputStream checkedMp4(InputStream content) {
        BufferedInputStream buffered = new BufferedInputStream(content);
        try {
            buffered.mark(MP4_HEADER_LENGTH);
            byte[] header = buffered.readNBytes(MP4_HEADER_LENGTH);
            buffered.reset();
            if (header.length < MP4_HEADER_LENGTH
                    || !Arrays.equals(header, 4, MP4_HEADER_LENGTH, MP4_SIGNATURE, 0, MP4_SIGNATURE.length)) {
                throw new InvalidVideoException("The file is not a valid MP4 video");
            }
            return buffered;
        } catch (IOException e) {
            throw new VideoStorageException("Could not read the uploaded file", e);
        }
    }
}
