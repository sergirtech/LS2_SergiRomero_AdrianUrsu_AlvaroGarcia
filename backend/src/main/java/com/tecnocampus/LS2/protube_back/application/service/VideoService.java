package com.tecnocampus.LS2.protube_back.application.service;

import com.tecnocampus.LS2.protube_back.application.port.in.ListVideosUseCase;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VideoService implements ListVideosUseCase {

    @Override
    public List<String> getVideos() {
        return List.of("video1", "video2");
    }
}
