package com.tecnocampus.LS2.protube_back.infrastructure.persistence;

import com.tecnocampus.LS2.protube_back.application.port.out.PlaylistRepository;
import com.tecnocampus.LS2.protube_back.domain.exception.PlaylistNotFoundException;
import com.tecnocampus.LS2.protube_back.domain.exception.UserNotFoundException;
import com.tecnocampus.LS2.protube_back.domain.exception.VideoNotFoundException;
import com.tecnocampus.LS2.protube_back.domain.model.Playlist;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
class PlaylistPersistenceAdapter implements PlaylistRepository {
    private final PlaylistJpaRepository playlists;
    private final UserJpaRepository users;
    private final VideoJpaRepository videos;

    PlaylistPersistenceAdapter(PlaylistJpaRepository playlists, UserJpaRepository users, VideoJpaRepository videos) {
        this.playlists = playlists;
        this.users = users;
        this.videos = videos;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Playlist> findByOwnerId(Long ownerId) {
        return playlists.findAllByOwner_IdOrderByIdDesc(ownerId).stream().map(PlaylistJpaEntity::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Playlist> findById(Long playlistId) {
        return playlists.findWithVideosById(playlistId).map(PlaylistJpaEntity::toDomain);
    }

    @Override
    @Transactional
    public Playlist create(Long ownerId, String name) {
        UserJpaEntity owner = users.findById(ownerId).orElseThrow(() -> new UserNotFoundException("User not found"));
        return playlists.save(PlaylistJpaEntity.create(owner, name)).toDomain();
    }

    @Override
    @Transactional
    public Playlist updateName(Long playlistId, String name) {
        PlaylistJpaEntity playlist = getPlaylist(playlistId);
        playlist.rename(name);
        return playlists.save(playlist).toDomain();
    }

    @Override
    @Transactional
    public Playlist addVideo(Long playlistId, Long videoId) {
        PlaylistJpaEntity playlist = getPlaylist(playlistId);
        VideoJpaEntity video = videos.findWithOwnerById(videoId).orElseThrow(() -> new VideoNotFoundException(videoId));
        playlist.addVideo(video);
        return playlists.save(playlist).toDomain();
    }

    @Override
    @Transactional
    public Playlist removeVideo(Long playlistId, Long videoId) {
        PlaylistJpaEntity playlist = getPlaylist(playlistId);
        playlist.removeVideo(videoId);
        return playlists.save(playlist).toDomain();
    }

    @Override
    @Transactional
    public void delete(Long playlistId) {
        playlists.delete(getPlaylist(playlistId));
    }

    @Override
    @Transactional
    public void removeVideoFromAll(Long videoId) {
        playlists.findAll().forEach(playlist -> playlist.removeVideo(videoId));
    }

    private PlaylistJpaEntity getPlaylist(Long id) {
        return playlists.findWithVideosById(id).orElseThrow(() -> new PlaylistNotFoundException(id));
    }
}
