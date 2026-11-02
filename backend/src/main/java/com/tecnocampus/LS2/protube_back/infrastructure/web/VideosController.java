package com.tecnocampus.LS2.protube_back.infrastructure.web;

import com.tecnocampus.LS2.protube_back.application.port.in.ListVideosUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/videos")
public class VideosController {

    private final ListVideosUseCase listVideosUseCase;

    public VideosController(ListVideosUseCase listVideosUseCase) {
        this.listVideosUseCase = listVideosUseCase;
    }

    @GetMapping("")
    public ResponseEntity<List<String>> getVideos() {
        return ResponseEntity.ok().body(listVideosUseCase.getVideos());
    }
}
