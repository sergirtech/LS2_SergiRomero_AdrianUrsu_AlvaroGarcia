package com.tecnocampus.LS2.protube_back.application.service;

import com.tecnocampus.LS2.protube_back.application.port.in.ListVideosUseCase;
import com.tecnocampus.LS2.protube_back.application.port.out.VideoMetadataSource;
import com.tecnocampus.LS2.protube_back.application.port.out.VideoRepository;
import com.tecnocampus.LS2.protube_back.domain.exception.VideoNotFoundException;
import com.tecnocampus.LS2.protube_back.domain.model.PageResult;
import com.tecnocampus.LS2.protube_back.domain.model.Video;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.tecnocampus.LS2.protube_back.domain.model.VideoFixtures.video;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoServiceTest {

    @Mock
    VideoRepository videoRepository;

    @Mock
    VideoMetadataSource metadataSource;

    @InjectMocks
    VideoService videoService;

    @Test
    void listVideosClampsNegativePageAndTooBigSize() {
        PageResult<Video> empty = new PageResult<>(List.of(), 0, ListVideosUseCase.MAX_PAGE_SIZE, 0);
        when(videoRepository.findPage(0, ListVideosUseCase.MAX_PAGE_SIZE)).thenReturn(empty);

        assertSame(empty, videoService.listVideos(-3, 1000));
    }

    @Test
    void listVideosUsesAtLeastOneItemPerPage() {
        PageResult<Video> empty = new PageResult<>(List.of(), 2, 1, 0);
        when(videoRepository.findPage(2, 1)).thenReturn(empty);

        assertSame(empty, videoService.listVideos(2, 0));
    }

    @Test
    void getVideoReturnsStoredVideo() {
        Video video = video(7L, "7");
        when(videoRepository.findById(7L)).thenReturn(Optional.of(video));

        assertSame(video, videoService.getVideo(7L));
    }

    @Test
    void getVideoThrowsWhenMissing() {
        when(videoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(VideoNotFoundException.class, () -> videoService.getVideo(99L));
    }

    @Test
    void importSavesOnlyVideosNotStoredYet() {
        Video existing = video(null, "0");
        Video fresh = video(null, "1");
        when(metadataSource.readAll()).thenReturn(List.of(existing, fresh));
        when(videoRepository.existsByFileKey("0")).thenReturn(true);
        when(videoRepository.existsByFileKey("1")).thenReturn(false);

        assertEquals(1, videoService.importFromStore());
        verify(videoRepository).save(fresh);
        verify(videoRepository, never()).save(existing);
    }
}
