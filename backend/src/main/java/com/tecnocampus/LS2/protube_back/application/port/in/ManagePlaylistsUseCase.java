package com.tecnocampus.LS2.protube_back.application.port.in;

import com.tecnocampus.LS2.protube_back.domain.model.Playlist;

import java.util.List;

public interface ManagePlaylistsUseCase {
    List<Playlist> list(String ownerEmail);

    Playlist create(String ownerEmail, String name);

    Playlist rename(String ownerEmail, Long playlistId, String name);

    Playlist addVideo(String ownerEmail, Long playlistId, Long videoId);

    Playlist removeVideo(String ownerEmail, Long playlistId, Long videoId);

    void delete(String ownerEmail, Long playlistId);
}
