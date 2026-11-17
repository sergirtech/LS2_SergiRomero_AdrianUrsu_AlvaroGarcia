package com.tecnocampus.LS2.protube_back.application.service;

import com.tecnocampus.LS2.protube_back.application.port.in.FindUserUseCase;
import com.tecnocampus.LS2.protube_back.application.port.out.CommentRepository;
import com.tecnocampus.LS2.protube_back.domain.exception.CommentNotFoundException;
import com.tecnocampus.LS2.protube_back.domain.exception.InvalidCommentException;
import com.tecnocampus.LS2.protube_back.domain.exception.VideoNotFoundException;
import com.tecnocampus.LS2.protube_back.domain.model.User;
import com.tecnocampus.LS2.protube_back.domain.model.VideoComment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {
    private static final String EMAIL = "owner@example.com";
    private static final User OWNER = new User(1L, EMAIL, "owner", "hash");
    @Mock CommentRepository comments;
    @Mock FindUserUseCase users;
    @InjectMocks CommentService service;

    @Test
    void listsPublicAndOwnedComments() {
        when(comments.videoExists(2L)).thenReturn(true);
        when(comments.findByVideoId(2L, null)).thenReturn(List.of());
        when(users.getByEmail(EMAIL)).thenReturn(OWNER);
        when(comments.findByAuthorId(1L)).thenReturn(List.of());

        assertEquals(List.of(), service.listForVideo(2L, null));
        assertEquals(List.of(), service.listForAuthor(EMAIL));
    }

    @Test
    void rejectsCommentsForUnknownVideos() {
        when(comments.videoExists(3L)).thenReturn(false);
        assertThrows(VideoNotFoundException.class, () -> service.listForVideo(3L, null));
        verify(comments, never()).findByVideoId(any(), any());
    }

    @Test
    void createsRepliesOnlyOnTheSameVideo() {
        VideoComment parent = comment(7L, 2L, 1L, null);
        when(users.getByEmail(EMAIL)).thenReturn(OWNER);
        when(comments.videoExists(2L)).thenReturn(true);
        when(comments.findById(7L, 1L)).thenReturn(Optional.of(parent));
        when(comments.save(1L, 2L, 7L, "reply")).thenReturn(comment(8L, 2L, 1L, 7L));

        assertEquals(8L, service.create(EMAIL, 2L, 7L, " reply ").id());
        assertThrows(InvalidCommentException.class, () -> service.create(EMAIL, 2L, 7L, " "));
    }

    @Test
    void deniesEditingAndDeletingOtherUsersComments() {
        when(users.getByEmail(EMAIL)).thenReturn(OWNER);
        when(comments.findById(4L, 1L)).thenReturn(Optional.of(comment(4L, 2L, 9L, null)));

        assertThrows(CommentNotFoundException.class, () -> service.edit(EMAIL, 4L, "changed"));
        assertThrows(CommentNotFoundException.class, () -> service.delete(EMAIL, 4L));
        verify(comments, never()).delete(4L);
    }

    @Test
    void togglesLikesOnlyForExistingComments() {
        when(users.getByEmail(EMAIL)).thenReturn(OWNER);
        when(comments.findById(4L, 1L)).thenReturn(Optional.of(comment(4L, 2L, 9L, null)));
        when(comments.toggleLike(4L, 1L)).thenReturn(true);
        assertEquals(true, service.toggleLike(EMAIL, 4L));
        when(comments.findById(5L, 1L)).thenReturn(Optional.empty());
        assertThrows(CommentNotFoundException.class, () -> service.toggleLike(EMAIL, 5L));
    }

    private static VideoComment comment(Long id, Long videoId, Long authorId, Long parentId) {
        return new VideoComment(id, videoId, "video", authorId, "owner", parentId, "text", Instant.EPOCH, 0,
                false);
    }
}
