package com.tecnocampus.LS2.protube_back.application.port.out;

import com.tecnocampus.LS2.protube_back.domain.model.Playlist;

import java.util.List;
import java.util.Optional;

public interface PlaylistRepository {
    List<Playlist> findByOwnerId(Long ownerId);

    Optional<Playlist> findById(Long playlistId);

    Playlist create(Long ownerId, String name);

    Playlist updateName(Long playlistId, String name);

    Playlist addVideo(Long playlistId, Long videoId);

    Playlist removeVideo(Long playlistId, Long videoId);

    void delete(Long playlistId);

    void removeVideoFromAll(Long videoId);
}
