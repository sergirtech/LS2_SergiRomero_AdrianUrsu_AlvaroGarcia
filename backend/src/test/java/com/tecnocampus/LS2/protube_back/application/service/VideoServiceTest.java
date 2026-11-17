package com.tecnocampus.LS2.protube_back.application.service;

import com.tecnocampus.LS2.protube_back.application.port.in.FindUserUseCase;
import com.tecnocampus.LS2.protube_back.application.port.out.VideoRepository;
import com.tecnocampus.LS2.protube_back.domain.exception.InvalidVideoException;
import com.tecnocampus.LS2.protube_back.domain.exception.VideoNotFoundException;
import com.tecnocampus.LS2.protube_back.domain.model.User;
import com.tecnocampus.LS2.protube_back.domain.model.Video;
import com.tecnocampus.LS2.protube_back.domain.model.VideoFile;
import com.tecnocampus.LS2.protube_back.domain.model.VideoSummary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VideoServiceTest {
    private static final String EMAIL = "creator@example.com";
    private static final User OWNER = new User(3L, EMAIL, "creator", "hash");
    private static final byte[] MP4 = new byte[]{0, 0, 0, 16, 'f', 't', 'y', 'p', 1};

    @Mock
    VideoRepository videoRepository;
    @Mock
    FindUserUseCase findUserUseCase;
    @InjectMocks
    VideoService videoService;

    @Test
    void firstCatalogPageUsesTwentyRecordsAndReturnsCursorOnlyWhenThereAreMore() {
        List<VideoSummary> results = summaries(21);
        when(videoRepository.findPage(null, 21)).thenReturn(results);

        var page = videoService.getVideos(null, 20);

        assertEquals(20, page.items().size());
        assertEquals(20L, page.nextCursor());
    }

    @Test
    void finalCatalogPageHasNoNextCursor() {
        when(videoRepository.findPage(20L, 11)).thenReturn(summaries(2));

        var page = videoService.getVideos(20L, 10);

        assertEquals(2, page.items().size());
        assertNull(page.nextCursor());
    }

    @Test
    void listsOnlyTheAuthenticatedUsersVideos() {
        when(findUserUseCase.getByEmail(EMAIL)).thenReturn(OWNER);
        when(videoRepository.findPageForOwner(OWNER.getId(), null, 2)).thenReturn(summaries(2));

        var page = videoService.getMyVideos(EMAIL, null, 1);

        assertEquals(1, page.items().size());
        assertEquals(1L, page.nextCursor());
        verify(videoRepository).findPageForOwner(OWNER.getId(), null, 2);
    }

    @Test
    void rejectsInvalidPageSizeAndCursor() {
        assertThrows(InvalidVideoException.class, () -> videoService.getVideos(null, 0));
        assertThrows(InvalidVideoException.class, () -> videoService.getVideos(null, 51));
        assertThrows(InvalidVideoException.class, () -> videoService.getVideos(0L, 20));
    }

    @Test
    void uploadsValidMp4ForAuthenticatedOwner() {
        when(findUserUseCase.getByEmail(EMAIL)).thenReturn(OWNER);
        when(videoRepository.save(org.mockito.ArgumentMatchers.any(Video.class)))
                .thenReturn(summary(51, OWNER.getId()));

        VideoSummary saved = videoService.upload(EMAIL, " A video ", " description ", "clip.MP4", MP4);

        ArgumentCaptor<Video> captured = ArgumentCaptor.forClass(Video.class);
        verify(videoRepository).save(captured.capture());
        assertEquals(51L, saved.id());
        assertEquals("A video", captured.getValue().getTitle());
        assertEquals("description", captured.getValue().getDescription());
        assertEquals(OWNER.getId(), captured.getValue().getOwnerId());
        assertEquals("video/mp4", captured.getValue().getContentType());
        assertArrayEquals(MP4, captured.getValue().getContent());
    }

    @Test
    void rejectsInvalidVideoFilesAndMetadata() {
        assertThrows(InvalidVideoException.class,
                () -> videoService.upload(EMAIL, "title", "description", "clip.mov", MP4));
        assertThrows(InvalidVideoException.class,
                () -> videoService.upload(EMAIL, "title", "description", "clip.mp4", new byte[]{1, 2, 3}));
        assertThrows(InvalidVideoException.class,
                () -> videoService.upload(EMAIL, " ", "description", "clip.mp4", MP4));
        verify(videoRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void editsAndDeletesOnlyTheOwnersVideo() {
        when(findUserUseCase.getByEmail(EMAIL)).thenReturn(OWNER);
        when(videoRepository.findSummaryById(12L)).thenReturn(Optional.of(summary(12, OWNER.getId())));
        when(videoRepository.updateMetadata(12L, "new title", ""))
                .thenReturn(new VideoSummary(12L, "new title", "", OWNER.getId(), OWNER.getUsername(),
                        Instant.now()));

        VideoSummary updated = videoService.edit(EMAIL, 12L, "new title", "");
        videoService.delete(EMAIL, 12L);

        assertEquals("new title", updated.title());
        verify(videoRepository).deleteById(12L);
    }

    @Test
    void hidesVideosOwnedByAnotherUser() {
        when(findUserUseCase.getByEmail(EMAIL)).thenReturn(OWNER);
        when(videoRepository.findSummaryById(12L)).thenReturn(Optional.of(summary(12, 4L)));

        assertThrows(VideoNotFoundException.class, () -> videoService.edit(EMAIL, 12L, "title", ""));
        assertThrows(VideoNotFoundException.class, () -> videoService.delete(EMAIL, 12L));
        verify(videoRepository, never()).updateMetadata(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        verify(videoRepository, never()).deleteById(12L);
    }

    @Test
    void readsPublicVideoMetadataAndFile() {
        VideoSummary expected = summary(8, OWNER.getId());
        VideoFile file = new VideoFile("video/mp4", MP4);
        when(videoRepository.findSummaryById(8L)).thenReturn(Optional.of(expected));
        when(videoRepository.findFileById(8L)).thenReturn(Optional.of(file));

        assertEquals(expected, videoService.getVideo(8L));
        assertArrayEquals(MP4, videoService.getVideoFile(8L).content());
    }

    @Test
    void unknownVideoMetadataAndFileReturnNotFound() {
        when(videoRepository.findSummaryById(99L)).thenReturn(Optional.empty());
        when(videoRepository.findFileById(99L)).thenReturn(Optional.empty());

        assertThrows(VideoNotFoundException.class, () -> videoService.getVideo(99L));
        assertThrows(VideoNotFoundException.class, () -> videoService.getVideoFile(99L));
    }

    private static List<VideoSummary> summaries(int count) {
        List<VideoSummary> results = new ArrayList<>();
        for (long id = 1; id <= count; id++) results.add(summary(id, OWNER.getId()));
        return results;
    }

    private static VideoSummary summary(long id, long ownerId) {
        return new VideoSummary(id, "video " + id, "description", ownerId, "creator", Instant.EPOCH);
    }
}
