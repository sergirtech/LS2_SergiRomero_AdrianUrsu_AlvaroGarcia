package com.tecnocampus.LS2.protube_back.infrastructure.web;

import com.tecnocampus.LS2.protube_back.application.port.in.GetVideoUseCase;
import com.tecnocampus.LS2.protube_back.application.port.in.ListVideosUseCase;
import com.tecnocampus.LS2.protube_back.domain.model.PageResult;
import com.tecnocampus.LS2.protube_back.infrastructure.web.dto.VideoDetailResponse;
import com.tecnocampus.LS2.protube_back.infrastructure.web.dto.VideoPageResponse;
import com.tecnocampus.LS2.protube_back.infrastructure.web.dto.VideoSummaryResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static com.tecnocampus.LS2.protube_back.domain.model.VideoFixtures.video;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ExtendWith(MockitoExtension.class)
class VideosControllerTest {

    @Mock
    ListVideosUseCase listVideosUseCase;

    @Mock
    GetVideoUseCase getVideoUseCase;

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
    }

    @Test
    void publishedAtIsSerializedAsIsoString() throws Exception {
        when(getVideoUseCase.getVideo(3L)).thenReturn(video(3L, "2"));

        // Standalone MockMvc uses the same default converters as the app (MvcConfig has @EnableWebMvc).
        MockMvcBuilders.standaloneSetup(videosController).build()
                .perform(get("/api/videos/3"))
                .andExpect(jsonPath("$.publishedAt").value("1970-01-01T00:00:00Z"));
    }
}
