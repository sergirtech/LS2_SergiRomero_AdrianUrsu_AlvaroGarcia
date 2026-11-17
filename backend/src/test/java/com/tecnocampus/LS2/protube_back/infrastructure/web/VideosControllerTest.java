package com.tecnocampus.LS2.protube_back.infrastructure.web;

import com.tecnocampus.LS2.protube_back.application.port.in.DeleteVideoUseCase;
import com.tecnocampus.LS2.protube_back.application.port.in.EditVideoUseCase;
import com.tecnocampus.LS2.protube_back.application.port.in.GetVideoFileUseCase;
import com.tecnocampus.LS2.protube_back.application.port.in.GetVideoUseCase;
import com.tecnocampus.LS2.protube_back.application.port.in.ListVideosUseCase;
import com.tecnocampus.LS2.protube_back.application.port.in.ListMyVideosUseCase;
import com.tecnocampus.LS2.protube_back.application.port.in.UploadVideoUseCase;
import com.tecnocampus.LS2.protube_back.domain.model.VideoFile;
import com.tecnocampus.LS2.protube_back.domain.model.VideoPage;
import com.tecnocampus.LS2.protube_back.domain.model.VideoSummary;
import com.tecnocampus.LS2.protube_back.infrastructure.web.dto.VideoUpdateRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VideosControllerTest {
    private static final byte[] MP4 = new byte[]{0, 0, 0, 16, 'f', 't', 'y', 'p'};

    @Mock
    ListVideosUseCase listVideosUseCase;
    @Mock
    ListMyVideosUseCase listMyVideosUseCase;
    @Mock
    UploadVideoUseCase uploadVideoUseCase;
    @Mock
    EditVideoUseCase editVideoUseCase;
    @Mock
    DeleteVideoUseCase deleteVideoUseCase;
    @Mock
    GetVideoUseCase getVideoUseCase;
    @Mock
    GetVideoFileUseCase getVideoFileUseCase;
    @Mock
    Authentication authentication;
    @InjectMocks
    VideosController videosController;

    @Test
    void returnsPaginatedVideoMetadata() {
        VideoSummary summary = summary(1L);
        when(listVideosUseCase.getVideos(20L, 10)).thenReturn(new VideoPage(List.of(summary), null));

        var response = videosController.getVideos(20L, 10);

        assertEquals(1L, response.items().get(0).id());
        assertEquals("creator", response.items().get(0).ownerUsername());
        assertNull(response.nextCursor());
    }

    @Test
    void defaultsToTwentyInitiallyAndTenForSubsequentPages() {
        when(listVideosUseCase.getVideos(null, 20)).thenReturn(new VideoPage(List.of(), null));
        when(listVideosUseCase.getVideos(8L, 10)).thenReturn(new VideoPage(List.of(), null));

        videosController.getVideos(null, null);
        videosController.getVideos(8L, null);

        verify(listVideosUseCase).getVideos(null, 20);
        verify(listVideosUseCase).getVideos(8L, 10);
    }

    @Test
    void returnsAuthenticatedUsersVideoPage() {
        when(authentication.getName()).thenReturn("creator@example.com");
        when(listMyVideosUseCase.getMyVideos("creator@example.com", null, 20))
                .thenReturn(new VideoPage(List.of(summary(1L)), null));

        var response = videosController.getMyVideos(authentication, null, null);

        assertEquals(1L, response.items().get(0).id());
        verify(listMyVideosUseCase).getMyVideos("creator@example.com", null, 20);
    }

    @Test
    void returnsVideoDetailsAndInlineFile() {
        when(getVideoUseCase.getVideo(1L)).thenReturn(summary(1L));
        when(getVideoFileUseCase.getVideoFile(1L)).thenReturn(new VideoFile("video/mp4", MP4));

        assertEquals("title", videosController.getVideo(1L).title());
        var response = videosController.getVideoFile(1L);

        assertEquals("video/mp4", response.getHeaders().getContentType().toString());
        assertArrayEquals(MP4, response.getBody());
    }

    @Test
    void uploadsVideoWithSessionIdentity() throws Exception {
        when(authentication.getName()).thenReturn("creator@example.com");
        when(uploadVideoUseCase.upload("creator@example.com", "title", "description", "clip.mp4", MP4))
                .thenReturn(summary(2L));
        MockMultipartFile file = new MockMultipartFile("file", "clip.mp4", "video/mp4", MP4);

        var response = videosController.uploadVideo(authentication, "title", "description", file);

        assertEquals(201, response.getStatusCode().value());
        assertEquals("/api/videos/2", response.getHeaders().getLocation().getPath());
    }

    @Test
    void editsAndDeletesVideoUsingSessionIdentity() {
        when(authentication.getName()).thenReturn("creator@example.com");
        when(editVideoUseCase.edit("creator@example.com", 1L, "updated", "description"))
                .thenReturn(summary(1L));

        assertEquals("title", videosController.editVideo(authentication, 1L,
                new VideoUpdateRequest("updated", "description")).title());
        assertEquals(204, videosController.deleteVideo(authentication, 1L).getStatusCode().value());
        verify(deleteVideoUseCase).delete("creator@example.com", 1L);
    }

    private static VideoSummary summary(Long id) {
        return new VideoSummary(id, "title", "description", 3L, "creator", Instant.EPOCH);
    }
}
