package com.tecnocampus.LS2.protube_back.infrastructure.persistence;

import com.tecnocampus.LS2.protube_back.application.port.out.CommentRepository;
import com.tecnocampus.LS2.protube_back.application.port.out.VideoRepository;
import com.tecnocampus.LS2.protube_back.domain.model.User;
import com.tecnocampus.LS2.protube_back.domain.model.Video;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@DataJpaTest
@Import({CommentPersistenceAdapter.class, PlaylistPersistenceAdapter.class, VideoPersistenceAdapter.class})
class CommentPersistenceAdapterTest {
    @Autowired CommentRepository comments;
    @Autowired VideoRepository videos;
    @Autowired UserJpaRepository users;

    @Test
    void compositeLikeIdentifierImplementsEqualityAndHashing() {
        var first = new CommentLikeJpaEntity.LikeId();
        var second = new CommentLikeJpaEntity.LikeId();

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertNotEquals(first, new Object());
    }

    @Test
    void persistsRepliesAndTogglesPerUserLikes() {
        UserJpaEntity owner = saveUser("owner@example.com", "owner");
        UserJpaEntity secondUser = saveUser("second@example.com", "second");
        var video = videos.save(new Video(null, "Title", "Description", owner.getId(), owner.getUsername(),
                "video/mp4", new byte[]{0, 0, 0, 16, 'f', 't', 'y', 'p'}, Instant.now()));
        var parent = comments.save(owner.getId(), video.id(), null, "Root comment");
        var reply = comments.save(secondUser.getId(), video.id(), parent.id(), "Reply");

        assertEquals(parent.id(), reply.parentId());
        assertEquals(2, comments.findByVideoId(video.id(), owner.getId()).size());
        assertTrue(comments.toggleLike(parent.id(), secondUser.getId()));
        assertFalse(comments.toggleLike(parent.id(), secondUser.getId()));
        assertEquals(0, comments.findById(parent.id(), owner.getId()).orElseThrow().likeCount());
    }

    @Test
    void listsCommentsByAuthorAndDeletesVideoWithItsComments() {
        UserJpaEntity owner = saveUser("owner2@example.com", "owner2");
        var video = videos.save(new Video(null, "Title", "Description", owner.getId(), owner.getUsername(),
                "video/mp4", new byte[]{0, 0, 0, 16, 'f', 't', 'y', 'p'}, Instant.now()));
        var comment = comments.save(owner.getId(), video.id(), null, "Root comment");

        assertEquals(1, comments.findByAuthorId(owner.getId()).size());
        videos.deleteById(video.id());

        assertTrue(comments.findById(comment.id(), owner.getId()).isEmpty());
    }

    private UserJpaEntity saveUser(String email, String username) {
        return users.save(UserJpaEntity.fromDomain(User.register(email, username, "hash")));
    }
}
