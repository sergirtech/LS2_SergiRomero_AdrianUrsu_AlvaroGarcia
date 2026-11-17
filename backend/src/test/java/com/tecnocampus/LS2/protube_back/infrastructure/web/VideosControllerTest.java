package com.tecnocampus.LS2.protube_back.infrastructure.web;

import com.tecnocampus.LS2.protube_back.application.port.in.DeleteVideoUseCase;
import com.tecnocampus.LS2.protube_back.application.port.in.EditVideoUseCase;
import com.tecnocampus.LS2.protube_back.application.port.in.GetVideoUseCase;
import com.tecnocampus.LS2.protube_back.application.port.in.ListMyVideosUseCase;
import com.tecnocampus.LS2.protube_back.application.port.in.ListVideosUseCase;
import com.tecnocampus.LS2.protube_back.application.port.in.UploadVideoUseCase;
import com.tecnocampus.LS2.protube_back.application.port.in.UploadVideoUseCase.UploadVideoCommand;
import com.tecnocampus.LS2.protube_back.domain.exception.VideoStorageException;
import com.tecnocampus.LS2.protube_back.domain.model.PageResult;
import com.tecnocampus.LS2.protube_back.infrastructure.web.dto.VideoDetailResponse;
import com.tecnocampus.LS2.protube_back.infrastructure.web.dto.VideoPageResponse;
import com.tecnocampus.LS2.protube_back.infrastructure.web.dto.VideoSummaryResponse;
import com.tecnocampus.LS2.protube_back.infrastructure.web.dto.VideoUpdateRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static com.tecnocampus.LS2.protube_back.domain.model.VideoFixtures.ownedVideo;
import static com.tecnocampus.LS2.protube_back.domain.model.VideoFixtures.video;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ExtendWith(MockitoExtension.class)
class VideosControllerTest {

    private static final Authentication ANA = new TestingAuthenticationToken("ana@mail.com", null);

    @Mock
    ListVideosUseCase listVideosUseCase;

    @Mock
    GetVideoUseCase getVideoUseCase;

    @Mock
    ListMyVideosUseCase listMyVideosUseCase;

    @Mock
    UploadVideoUseCase uploadVideoUseCase;

    @Mock
    EditVideoUseCase editVideoUseCase;

    @Mock
    DeleteVideoUseCase deleteVideoUseCase;

    @InjectMocks
    VideosController videosController;

    @Test
    void listVideosReturnsPageOfSummaries() {
        when(listVideosUseCase.listVideos(1, 2))
                .thenReturn(new PageResult<>(List.of(video(3L, "2"), video(4L, "3")), 1, 2, 5));

        VideoPageResponse response = videosController.listVideos(1, 2);

        assertEquals(List.of(3L, 4L), response.items().stream().map(VideoSummaryResponse::id).toList());
        assertEquals("2", response.items().get(0).fileKey());
        assertEquals(5, response.totalItems());
        assertTrue(response.hasNext());
    }

    @Test
    void getVideoReturnsDetail() {
        when(getVideoUseCase.getVideo(3L)).thenReturn(video(3L, "2"));

        VideoDetailResponse response = videosController.getVideo(3L);

        assertEquals("Title 2", response.title());
        assertEquals("Description", response.description());
        assertNull(response.ownerId());
        assertEquals(List.of(), response.tags());
    }

    @Test
    void publishedAtIsSerializedAsIsoString() throws Exception {
        when(getVideoUseCase.getVideo(3L)).thenReturn(video(3L, "2"));

        // Standalone MockMvc uses the same default converters as the app (MvcConfig has @EnableWebMvc).
        MockMvcBuilders.standaloneSetup(videosController).build()
                .perform(get("/api/videos/3"))
                .andExpect(jsonPath("$.publishedAt").value("1970-01-01T00:00:00Z"));
    }

    @Test
    void listMyVideosUsesTheSessionUser() {
        when(listMyVideosUseCase.listMyVideos("ana@mail.com", 0, 12))
                .thenReturn(new PageResult<>(List.of(ownedVideo(5L, 1L)), 0, 12, 1));

        VideoPageResponse response = videosController.listMyVideos(ANA, 0, 12);

        assertEquals(List.of(5L), response.items().stream().map(VideoSummaryResponse::id).toList());
    }

    @Test
    void uploadAnswers201WithLocationAndTheNewVideo() {
        when(uploadVideoUseCase.upload(eq("ana@mail.com"), any())).thenReturn(ownedVideo(5L, 1L));
        MockMultipartFile file = new MockMultipartFile("file", "clip.mp4", "video/mp4", new byte[] {1, 2, 3});

        ResponseEntity<VideoDetailResponse> response =
                videosController.uploadVideo(ANA, "Title", "Text", List.of("rock"), "Music", file);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("/api/videos/5", response.getHeaders().getLocation().toString());
        assertEquals(1L, response.getBody().ownerId());
        assertEquals(List.of("music"), response.getBody().tags());

        ArgumentCaptor<UploadVideoCommand> command = ArgumentCaptor.forClass(UploadVideoCommand.class);
        verify(uploadVideoUseCase).upload(eq("ana@mail.com"), command.capture());
        assertEquals("Title", command.getValue().title());
        assertEquals(List.of("rock"), command.getValue().tags());
        assertEquals("Music", command.getValue().category());
        assertEquals("clip.mp4", command.getValue().fileName());
        assertEquals(3, command.getValue().sizeBytes());
    }

    @Test
    void anUnreadableUploadIsAStorageError() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream()).thenThrow(new IOException("gone"));

        assertThrows(VideoStorageException.class,
                () -> videosController.uploadVideo(ANA, "Title", "", null, null, file));
    }

    @Test
    void editPassesTheNewMetadata() {
        when(editVideoUseCase.edit("ana@mail.com", 5L, "New", "Text", List.of("a"), "Music"))
                .thenReturn(ownedVideo(5L, 1L).withMetadata("New", "Text", List.of("a"), "Music"));

        VideoDetailResponse response = videosController.editVideo(ANA, 5L,
                new VideoUpdateRequest("New", "Text", List.of("a"), "Music"));

        assertEquals("New", response.title());
        assertEquals("Music", response.category());
    }

    @Test
    void deleteAnswers204() {
        ResponseEntity<Void> response = videosController.deleteVideo(ANA, 5L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(deleteVideoUseCase).delete("ana@mail.com", 5L);
    }
}
