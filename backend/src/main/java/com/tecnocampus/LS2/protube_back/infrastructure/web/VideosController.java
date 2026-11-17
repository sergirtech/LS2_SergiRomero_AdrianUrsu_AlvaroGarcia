package com.tecnocampus.LS2.protube_back.infrastructure.web;

import com.tecnocampus.LS2.protube_back.application.port.in.GetVideoUseCase;
import com.tecnocampus.LS2.protube_back.application.port.in.ListVideosUseCase;
import com.tecnocampus.LS2.protube_back.infrastructure.web.dto.VideoDetailResponse;
import com.tecnocampus.LS2.protube_back.infrastructure.web.dto.VideoPageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/videos")
public class VideosController {

    private final ListVideosUseCase listVideosUseCase;
    private final GetVideoUseCase getVideoUseCase;

    public VideosController(ListVideosUseCase listVideosUseCase, GetVideoUseCase getVideoUseCase) {
        this.listVideosUseCase = listVideosUseCase;
        this.getVideoUseCase = getVideoUseCase;
    }

    @GetMapping
    public VideoPageResponse listVideos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + ListVideosUseCase.DEFAULT_PAGE_SIZE) int size) {
        return VideoPageResponse.from(listVideosUseCase.listVideos(page, size));
    }

    @GetMapping("/{id}")
    public VideoDetailResponse getVideo(@PathVariable Long id) {
        return VideoDetailResponse.from(getVideoUseCase.getVideo(id));
    }
}
