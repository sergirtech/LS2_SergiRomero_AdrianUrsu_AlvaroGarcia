package com.tecnocampus.LS2.protube_back.infrastructure.web;

import com.tecnocampus.LS2.protube_back.application.port.in.ManagePlaylistsUseCase;
import com.tecnocampus.LS2.protube_back.infrastructure.web.dto.PlaylistNameRequest;
import com.tecnocampus.LS2.protube_back.infrastructure.web.dto.PlaylistResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistsController {
    private final ManagePlaylistsUseCase playlists;

    public PlaylistsController(ManagePlaylistsUseCase playlists) {
        this.playlists = playlists;
    }

    @GetMapping
    public List<PlaylistResponse> list(Authentication authentication) {
        return playlists.list(authentication.getName()).stream().map(PlaylistResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlaylistResponse create(Authentication authentication, @Valid @RequestBody PlaylistNameRequest request) {
        return PlaylistResponse.from(playlists.create(authentication.getName(), request.name()));
    }

    @PutMapping("/{playlistId}")
    public PlaylistResponse rename(Authentication authentication, @PathVariable Long playlistId,
                                   @Valid @RequestBody PlaylistNameRequest request) {
        return PlaylistResponse.from(playlists.rename(authentication.getName(), playlistId, request.name()));
    }

    @PostMapping("/{playlistId}/videos/{videoId}")
    public PlaylistResponse addVideo(Authentication authentication, @PathVariable Long playlistId,
                                     @PathVariable Long videoId) {
        return PlaylistResponse.from(playlists.addVideo(authentication.getName(), playlistId, videoId));
    }

    @DeleteMapping("/{playlistId}/videos/{videoId}")
    public PlaylistResponse removeVideo(Authentication authentication, @PathVariable Long playlistId,
                                        @PathVariable Long videoId) {
        return PlaylistResponse.from(playlists.removeVideo(authentication.getName(), playlistId, videoId));
    }

    @DeleteMapping("/{playlistId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Authentication authentication, @PathVariable Long playlistId) {
        playlists.delete(authentication.getName(), playlistId);
    }
}
