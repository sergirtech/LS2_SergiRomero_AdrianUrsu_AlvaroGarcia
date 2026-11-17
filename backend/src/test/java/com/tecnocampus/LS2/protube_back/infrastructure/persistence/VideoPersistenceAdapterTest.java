package com.tecnocampus.LS2.protube_back.infrastructure.persistence;

import com.tecnocampus.LS2.protube_back.application.port.out.VideoRepository;
import com.tecnocampus.LS2.protube_back.application.port.out.PlaylistRepository;
import com.tecnocampus.LS2.protube_back.domain.model.User;
import com.tecnocampus.LS2.protube_back.domain.model.Video;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/** Tests metadata and binary storage through the video output port on embedded H2. */
@DataJpaTest
@Import({CommentPersistenceAdapter.class, PlaylistPersistenceAdapter.class, VideoPersistenceAdapter.class})
class VideoPersistenceAdapterTest {
    private static final byte[] CONTENT = new byte[]{0, 0, 0, 16, 'f', 't', 'y', 'p', 1};

    @Autowired
    VideoRepository videoRepository;
    @Autowired
    PlaylistRepository playlistRepository;
    @Autowired
    UserJpaRepository userJpaRepository;

    @Test
    void savesMetadataAndBinaryContentSeparately() {
        UserJpaEntity owner = saveOwner();

        var saved = videoRepository.save(new Video(null, "Title", "Description", owner.getId(),
                owner.getUsername(), "video/mp4", CONTENT, Instant.EPOCH));

        assertNotNull(saved.id());
        assertEquals(owner.getId(), saved.ownerId());
        assertEquals("creator", saved.ownerUsername());
        assertArrayEquals(CONTENT, videoRepository.findFileById(saved.id()).orElseThrow().content());
        assertEquals("video/mp4", videoRepository.findFileById(saved.id()).orElseThrow().contentType());
    }

    @Test
    void listsByDescendingIdWithCursorAndUpdatesMetadata() {
        UserJpaEntity owner = saveOwner();
        var first = saveVideo(owner, "first");
        var second = saveVideo(owner, "second");
        var third = saveVideo(owner, "third");

        var firstPage = videoRepository.findPage(null, 2);
        var nextPage = videoRepository.findPage(firstPage.get(1).id(), 2);

        assertEquals(third.id(), firstPage.get(0).id());
        assertEquals(second.id(), firstPage.get(1).id());
        assertEquals(first.id(), nextPage.get(0).id());
        assertEquals(1, nextPage.size());
        var updated = videoRepository.updateMetadata(first.id(), "renamed", "updated");
        assertEquals("renamed", updated.title());
        assertEquals("updated", updated.description());
    }

    @Test
    void deletesVideoContentAndMetadata() {
        UserJpaEntity owner = saveOwner();
        var saved = saveVideo(owner, "to delete");
        var playlist = playlistRepository.create(owner.getId(), "Saved videos");
        playlistRepository.addVideo(playlist.id(), saved.id());

        videoRepository.deleteById(saved.id());

        assertFalse(videoRepository.findSummaryById(saved.id()).isPresent());
        assertFalse(videoRepository.findFileById(saved.id()).isPresent());
        assertEquals(java.util.List.of(), playlistRepository.findById(playlist.id()).orElseThrow().videoIds());
    }

    @Test
    void ownerCatalogDoesNotIncludeOtherUsersVideos() {
        UserJpaEntity owner = saveOwner();
        UserJpaEntity otherOwner = userJpaRepository.save(UserJpaEntity.fromDomain(
                User.register("other@example.com", "other", "hashed")));
        var ownVideo = saveVideo(owner, "mine");
        saveVideo(otherOwner, "theirs");

        var page = videoRepository.findPageForOwner(owner.getId(), null, 10);

        assertEquals(1, page.size());
        assertEquals(ownVideo.id(), page.get(0).id());
    }

    private UserJpaEntity saveOwner() {
        return userJpaRepository.save(UserJpaEntity.fromDomain(
                User.register("creator@example.com", "creator", "hashed")));
    }

    private com.tecnocampus.LS2.protube_back.domain.model.VideoSummary saveVideo(UserJpaEntity owner, String title) {
        return videoRepository.save(new Video(null, title, "description", owner.getId(), owner.getUsername(),
                "video/mp4", CONTENT, Instant.now()));
    }
}
