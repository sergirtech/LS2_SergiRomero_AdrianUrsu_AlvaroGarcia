package com.tecnocampus.LS2.protube_back.infrastructure.persistence;

import com.tecnocampus.LS2.protube_back.application.port.out.CommentRepository;
import com.tecnocampus.LS2.protube_back.application.port.out.VideoRepository;
import com.tecnocampus.LS2.protube_back.domain.model.Comment;
import com.tecnocampus.LS2.protube_back.domain.model.Video;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.List;

import static com.tecnocampus.LS2.protube_back.domain.model.VideoFixtures.video;
import static org.junit.jupiter.api.Assertions.*;

/** Tests the JPA adapter through the {@link CommentRepository} port, against embedded H2. */
@DataJpaTest
@Import({CommentPersistenceAdapter.class, VideoPersistenceAdapter.class})
class CommentPersistenceAdapterTest {

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    VideoRepository videoRepository;

    @Autowired
    TestEntityManager entityManager;

    Video video;
    Video otherVideo;

    @BeforeEach
    void saveVideos() {
        video = videoRepository.save(video(null, "0"));
        otherVideo = videoRepository.save(video(null, "1"));
    }

    private Comment written(Long videoId, String text, long second) {
        return new Comment(null, videoId, 7L, "ana", text, Instant.ofEpochSecond(second));
    }

    @Test
    void savesAComment() {
        Comment saved = commentRepository.save(written(video.id(), "Hi", 10));
        entityManager.flush();
        entityManager.clear();

        assertNotNull(saved.id());
        assertEquals(List.of(saved), commentRepository.findByVideoId(video.id()));
    }

    @Test
    void listsTheCommentsOfOneVideoNewestFirst() {
        commentRepository.saveAll(List.of(written(video.id(), "old", 10), written(video.id(), "new", 30),
                written(video.id(), "middle", 20), written(video.id(), "new, saved later", 30)));
        commentRepository.save(written(otherVideo.id(), "other video", 40));

        assertEquals(List.of("new, saved later", "new", "middle", "old"),
                commentRepository.findByVideoId(video.id()).stream().map(Comment::text).toList());
    }

    @Test
    void knowsWhichVideosHaveImportedComments() {
        commentRepository.save(written(video.id(), "by a user", 10));
        commentRepository.save(Comment.imported(otherVideo.id(), "someone", "imported", Instant.EPOCH));

        assertFalse(commentRepository.existsImportedByVideoId(video.id()));
        assertTrue(commentRepository.existsImportedByVideoId(otherVideo.id()));
    }

    @Test
    void deletingAVideoDeletesItsCommentsOnly() {
        commentRepository.save(written(video.id(), "deleted", 10));
        commentRepository.save(written(otherVideo.id(), "kept", 10));
        entityManager.flush();
        entityManager.clear();

        videoRepository.deleteById(video.id());
        entityManager.flush();
        entityManager.clear();

        assertEquals(List.of(), commentRepository.findByVideoId(video.id()));
        assertEquals(1, commentRepository.findByVideoId(otherVideo.id()).size());
    }

    @Test
    void findsAVideoByItsFileKey() {
        assertEquals(video.id(), videoRepository.findByFileKey("0").orElseThrow().id());
        assertTrue(videoRepository.findByFileKey("missing").isEmpty());
    }
}
