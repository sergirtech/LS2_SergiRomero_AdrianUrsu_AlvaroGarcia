package com.tecnocampus.LS2.protube_back.domain.model;

import java.util.List;

public record Playlist(Long id, Long ownerId, String name, List<Long> videoIds) {
    public Playlist {
        videoIds = List.copyOf(videoIds);
    }
}
