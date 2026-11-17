package com.tecnocampus.LS2.protube_back.application.service;

import com.tecnocampus.LS2.protube_back.application.port.in.FindUserUseCase;
import com.tecnocampus.LS2.protube_back.application.port.in.ManagePlaylistsUseCase;
import com.tecnocampus.LS2.protube_back.application.port.out.PlaylistRepository;
import com.tecnocampus.LS2.protube_back.domain.exception.InvalidPlaylistException;
import com.tecnocampus.LS2.protube_back.domain.exception.PlaylistNotFoundException;
import com.tecnocampus.LS2.protube_back.domain.model.Playlist;
import com.tecnocampus.LS2.protube_back.domain.model.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlaylistService implements ManagePlaylistsUseCase {
    private static final int MAX_NAME_LENGTH = 80;
    private final PlaylistRepository playlists;
    private final FindUserUseCase users;

    public PlaylistService(PlaylistRepository playlists, FindUserUseCase users) {
        this.playlists = playlists;
        this.users = users;
    }

    @Override
    public List<Playlist> list(String ownerEmail) {
        return playlists.findByOwnerId(users.getByEmail(ownerEmail).getId());
    }

    @Override
    public Playlist create(String ownerEmail, String name) {
        User owner = users.getByEmail(ownerEmail);
        return playlists.create(owner.getId(), validateName(name));
    }

    @Override
    public Playlist rename(String ownerEmail, Long playlistId, String name) {
        getOwned(ownerEmail, playlistId);
        return playlists.updateName(playlistId, validateName(name));
    }

    @Override
    public Playlist addVideo(String ownerEmail, Long playlistId, Long videoId) {
        getOwned(ownerEmail, playlistId);
        return playlists.addVideo(playlistId, videoId);
    }

    @Override
    public Playlist removeVideo(String ownerEmail, Long playlistId, Long videoId) {
        getOwned(ownerEmail, playlistId);
        return playlists.removeVideo(playlistId, videoId);
    }

    @Override
    public void delete(String ownerEmail, Long playlistId) {
        getOwned(ownerEmail, playlistId);
        playlists.delete(playlistId);
    }

    private Playlist getOwned(String email, Long id) {
        Long ownerId = users.getByEmail(email).getId();
        Playlist playlist = playlists.findById(id).orElseThrow(() -> new PlaylistNotFoundException(id));
        if (!playlist.ownerId().equals(ownerId)) throw new PlaylistNotFoundException(id);
        return playlist;
    }

    private static String validateName(String name) {
        if (name == null || name.isBlank() || name.trim().length() > MAX_NAME_LENGTH) {
            throw new InvalidPlaylistException("Playlist name must contain 1 to " + MAX_NAME_LENGTH + " characters");
        }
        return name.trim();
    }
}
