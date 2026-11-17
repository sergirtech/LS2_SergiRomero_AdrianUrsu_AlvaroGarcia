package com.tecnocampus.LS2.protube_back.infrastructure.web.dto;

import com.tecnocampus.LS2.protube_back.domain.model.Playlist;

import java.util.List;

public record PlaylistResponse(Long id, String name, List<Long> videoIds) {
    public static PlaylistResponse from(Playlist playlist) {
        return new PlaylistResponse(playlist.id(), playlist.name(), playlist.videoIds());
    }
}
