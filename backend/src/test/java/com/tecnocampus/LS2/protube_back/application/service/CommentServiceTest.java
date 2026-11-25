package com.tecnocampus.LS2.protube_back.application.service;

import com.tecnocampus.LS2.protube_back.application.port.out.CommentRepository;
import com.tecnocampus.LS2.protube_back.application.port.out.CommentSource;
import com.tecnocampus.LS2.protube_back.application.port.out.CommentSource.StoredComment;
import com.tecnocampus.LS2.protube_back.application.port.out.UserRepository;
import com.tecnocampus.LS2.protube_back.application.port.out.VideoRepository;
import com.tecnocampus.LS2.protube_back.domain.exception.InvalidCommentException;
import com.tecnocampus.LS2.protube_back.domain.exception.UserNotFoundException;
import com.tecnocampus.LS2.protube_back.domain.exception.VideoNotFoundException;
import com.tecnocampus.LS2.protube_back.domain.model.Comment;
import com.tecnocampus.LS2.protube_back.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.tecnocampus.LS2.protube_back.domain.model.VideoFixtures.video;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    private static final User ANA = new User(1L, "ana@mail.com", "ana", "hash");

    @Mock
    CommentRepository commentRepository;

    @Mock
    VideoRepository videoRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    CommentSource commentSource;

    @InjectMocks
    CommentService commentService;

    @Test
    void listsTheCommentsOfAVideo() {
        List<Comment> comments = List.of(new Comment(9L, 5L, 1L, "ana", "Hi", Instant.EPOCH));
        when(videoRepository.findById(5L)).thenReturn(Optional.of(video(5L, "4")));
        when(commentRepository.findByVideoId(5L)).thenReturn(comments);

        assertEquals(comments, commentService.listComments(5L));
    }

    @Test
    void commentsOfAnUnknownVideoAreNotFound() {
        when(videoRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(VideoNotFoundException.class, () -> commentService.listComments(5L));
        verifyNoInteractions(commentRepository);
    }

    @Test
    void addsACommentByTheSignedInUser() {
        when(userRepository.findByEmail("ana@mail.com")).thenReturn(Optional.of(ANA));
        when(videoRepository.findById(5L)).thenReturn(Optional.of(video(5L, "4")));
        when(commentRepository.save(any())).thenAnswer(call -> call.getArgument(0));

        Comment comment = commentService.addComment(" Ana@Mail.com ", 5L, " Hi ");

        assertEquals(5L, comment.videoId());
        assertEquals(1L, comment.authorId());
        assertEquals("ana", comment.authorName());
        assertEquals("Hi", comment.text());
    }

    @Test
    void invalidCommentsAreNotSaved() {
        when(userRepository.findByEmail("ana@mail.com")).thenReturn(Optional.of(ANA));
        when(videoRepository.findById(5L)).thenReturn(Optional.of(video(5L, "4")));
        when(videoRepository.findById(6L)).thenReturn(Optional.empty());

        assertThrows(InvalidCommentException.class, () -> commentService.addComment("ana@mail.com", 5L, "  "));
        assertThrows(VideoNotFoundException.class, () -> commentService.addComment("ana@mail.com", 6L, "Hi"));
        verify(commentRepository, never()).save(any());
    }

    @Test
    void aUserThatNoLongerExistsCannotComment() {
        when(userRepository.findByEmail("ana@mail.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> commentService.addComment("ana@mail.com", 5L, "Hi"));
        verifyNoInteractions(commentRepository);
    }

    @Test
    void importsTheStoreCommentsOfCatalogVideosOnlyOnce() {
        StoredComment stored = new StoredComment("someone", "Hello", Instant.EPOCH);
        when(commentSource.readAll()).thenReturn(Map.of(
                "new", List.of(stored, stored),
                "already-imported", List.of(stored),
                "not-in-catalog", List.of(stored)));
        when(videoRepository.findByFileKey("new")).thenReturn(Optional.of(video(5L, "new")));
        when(videoRepository.findByFileKey("already-imported")).thenReturn(Optional.of(video(6L, "already-imported")));
        when(videoRepository.findByFileKey("not-in-catalog")).thenReturn(Optional.empty());
        when(commentRepository.existsImportedByVideoId(5L)).thenReturn(false);
        when(commentRepository.existsImportedByVideoId(6L)).thenReturn(true);

        assertEquals(2, commentService.importCommentsFromStore());

        Comment expected = new Comment(null, 5L, null, "someone", "Hello", Instant.EPOCH);
        verify(commentRepository).saveAll(List.of(expected, expected));
    }
}
