package com.tecnocampus.LS2.protube_back.infrastructure.persistence;

import com.tecnocampus.LS2.protube_back.application.port.out.PlaylistRepository;
import com.tecnocampus.LS2.protube_back.domain.model.User;
import com.tecnocampus.LS2.protube_back.domain.model.Video;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@Import(PlaylistPersistenceAdapter.class)
class PlaylistPersistenceAdapterTest {
    @Autowired PlaylistRepository playlists;
    @Autowired UserJpaRepository users;
    @Autowired VideoJpaRepository videos;

    @Test
    void persistsOwnedPlaylistsAndVideoMembershipInOrder() {
        UserJpaEntity owner = saveUser("playlist-owner@example.com", "owner");
        UserJpaEntity other = saveUser("playlist-other@example.com", "other");
        VideoJpaEntity first = saveVideo(owner, "first");
        VideoJpaEntity second = saveVideo(owner, "second");
        var playlist = playlists.create(owner.getId(), "Road trip");
        playlists.addVideo(playlist.id(), first.getId());
        playlists.addVideo(playlist.id(), second.getId());

        assertEquals(List.of(playlist.id()), playlists.findByOwnerId(owner.getId()).stream()
                .map(item -> item.id()).toList());
        assertEquals(List.of(first.getId(), second.getId()), playlists.findById(playlist.id()).orElseThrow().videoIds());
        assertEquals(List.of(), playlists.findByOwnerId(other.getId()));

        playlists.updateName(playlist.id(), "Summer drive");
        assertEquals("Summer drive", playlists.findById(playlist.id()).orElseThrow().name());
        playlists.removeVideo(playlist.id(), first.getId());
        assertEquals(List.of(second.getId()), playlists.findById(playlist.id()).orElseThrow().videoIds());
        playlists.removeVideoFromAll(second.getId());
        assertEquals(List.of(), playlists.findById(playlist.id()).orElseThrow().videoIds());
        playlists.delete(playlist.id());
        assertEquals(List.of(), playlists.findByOwnerId(owner.getId()));
    }

    private UserJpaEntity saveUser(String email, String username) {
        return users.save(UserJpaEntity.fromDomain(User.register(email, username, "hash")));
    }

    private VideoJpaEntity saveVideo(UserJpaEntity owner, String title) {
        return videos.save(VideoJpaEntity.fromDomain(new Video(null, title, "description", owner.getId(),
                owner.getUsername(), "video/mp4", new byte[]{1, 2, 3}, Instant.EPOCH), owner));
    }
}
