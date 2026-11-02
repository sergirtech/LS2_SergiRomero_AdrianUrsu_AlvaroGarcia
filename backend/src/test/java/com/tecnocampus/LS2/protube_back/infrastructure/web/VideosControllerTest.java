package com.tecnocampus.LS2.protube_back.infrastructure.web;

import com.tecnocampus.LS2.protube_back.application.port.in.ListVideosUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VideosControllerTest {

    @Mock
    ListVideosUseCase listVideosUseCase;

    @InjectMocks
    VideosController videosController;

    @Test
    void getVideos() {
        when(listVideosUseCase.getVideos()).thenReturn(List.of("video 1", "video 2"));
        assertEquals(List.of("video 1", "video 2"), videosController.getVideos().getBody());
    }
}
