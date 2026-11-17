package com.tecnocampus.LS2.protube_back.infrastructure.persistence;

import com.tecnocampus.LS2.protube_back.application.port.out.VideoRepository;
import com.tecnocampus.LS2.protube_back.domain.model.PageResult;
import com.tecnocampus.LS2.protube_back.domain.model.Video;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.List;

import static com.tecnocampus.LS2.protube_back.domain.model.VideoFixtures.video;
import static org.junit.jupiter.api.Assertions.*;

/** Tests the JPA adapter through the {@link VideoRepository} port, against embedded H2. */
@DataJpaTest
@Import(VideoPersistenceAdapter.class)
class VideoPersistenceAdapterTest {

    @Autowired
    VideoRepository videoRepository;

    @Test
    void savesAndFindsVideoById() {
        Video saved = videoRepository.save(video(null, "0"));

        Video found = videoRepository.findById(saved.id()).orElseThrow();

        assertNotNull(saved.id());
        assertEquals("0", found.fileKey());
        assertEquals("Title 0", found.title());
        assertEquals(Instant.EPOCH, found.publishedAt());
    }

    @Test
    void findPageReturnsVideosInInsertionOrder() {
        for (int i = 0; i < 5; i++) {
            videoRepository.save(video(null, String.valueOf(i)));
        }

        PageResult<Video> page = videoRepository.findPage(1, 2);

        assertEquals(List.of("2", "3"), page.items().stream().map(Video::fileKey).toList());
        assertEquals(5, page.totalItems());
        assertTrue(page.hasNext());
    }

    @Test
    void existsByFileKey() {
        videoRepository.save(video(null, "0"));

        assertTrue(videoRepository.existsByFileKey("0"));
        assertFalse(videoRepository.existsByFileKey("1"));
    }

    private static Video uploaded(String fileKey, Long ownerId) {
        return new Video(null, fileKey, "Upload " + fileKey, "ana", "Text", 0, 0, 0, 0, 0, Instant.EPOCH, ownerId,
                List.of("rock", "live"), "Music");
    }

    @Test
    void savesOwnerTagsAndCategory() {
        Video saved = videoRepository.save(uploaded("upload-a", 7L));

        Video found = videoRepository.findById(saved.id()).orElseThrow();

        assertEquals(7L, found.ownerId());
        assertEquals(List.of("rock", "live"), found.tags());
        assertEquals("Music", found.category());
    }

    @Test
    void updatingTheTagsReplacesThem() {
        Video saved = videoRepository.save(uploaded("upload-a", 7L));

        videoRepository.save(saved.withMetadata("New", "Text", List.of("pop"), null));

        Video found = videoRepository.findById(saved.id()).orElseThrow();
        assertEquals("New", found.title());
        assertEquals(List.of("pop"), found.tags());
        assertNull(found.category());
    }

    @Test
    void findPageByOwnerReturnsOnlyThatOwnersVideosNewestFirst() {
        videoRepository.save(video(null, "0"));
        Video first = videoRepository.save(uploaded("upload-a", 7L));
        videoRepository.save(uploaded("upload-b", 8L));
        Video second = videoRepository.save(uploaded("upload-c", 7L));

        PageResult<Video> page = videoRepository.findPageByOwner(7L, 0, 10);

        assertEquals(List.of(second.id(), first.id()), page.items().stream().map(Video::id).toList());
        assertEquals(2, page.totalItems());
        assertFalse(page.hasNext());
    }

    @Test
    void deleteByIdRemovesTheVideo() {
        Video saved = videoRepository.save(uploaded("upload-a", 7L));

        videoRepository.deleteById(saved.id());

        assertTrue(videoRepository.findById(saved.id()).isEmpty());
        assertFalse(videoRepository.existsByFileKey("upload-a"));
    }
}
