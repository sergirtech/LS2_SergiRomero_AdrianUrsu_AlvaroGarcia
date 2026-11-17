package com.tecnocampus.LS2.protube_back.application.service;

import com.tecnocampus.LS2.protube_back.application.port.in.FindUserUseCase;
import com.tecnocampus.LS2.protube_back.application.port.in.ManageCommentsUseCase;
import com.tecnocampus.LS2.protube_back.application.port.out.CommentRepository;
import com.tecnocampus.LS2.protube_back.domain.exception.CommentNotFoundException;
import com.tecnocampus.LS2.protube_back.domain.exception.InvalidCommentException;
import com.tecnocampus.LS2.protube_back.domain.exception.VideoNotFoundException;
import com.tecnocampus.LS2.protube_back.domain.model.User;
import com.tecnocampus.LS2.protube_back.domain.model.VideoComment;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService implements ManageCommentsUseCase {
    private static final int MAX_TEXT_LENGTH = 1000;
    private final CommentRepository comments;
    private final FindUserUseCase users;

    public CommentService(CommentRepository comments, FindUserUseCase users) {
        this.comments = comments;
        this.users = users;
    }

    @Override
    public List<VideoComment> listForVideo(Long videoId, String viewerEmail) {
        if (!comments.videoExists(videoId)) throw new VideoNotFoundException(videoId);
        Long viewerId = viewerEmail == null ? null : users.getByEmail(viewerEmail).getId();
        return comments.findByVideoId(videoId, viewerId);
    }

    @Override
    public List<VideoComment> listForAuthor(String authorEmail) {
        User author = users.getByEmail(authorEmail);
        return comments.findByAuthorId(author.getId());
    }

    @Override
    public VideoComment create(String authorEmail, Long videoId, Long parentId, String text) {
        User author = users.getByEmail(authorEmail);
        if (!comments.videoExists(videoId)) throw new VideoNotFoundException(videoId);
        if (parentId != null) {
            VideoComment parent = comments.findById(parentId, author.getId())
                    .orElseThrow(() -> new CommentNotFoundException(parentId));
            if (!parent.videoId().equals(videoId)) {
                throw new InvalidCommentException("A reply must belong to the same video as its parent comment");
            }
        }
        return comments.save(author.getId(), videoId, parentId, validateText(text));
    }

    @Override
    public VideoComment edit(String authorEmail, Long commentId, String text) {
        User author = users.getByEmail(authorEmail);
        VideoComment comment = getOwnedComment(commentId, author.getId());
        return comments.update(comment.id(), validateText(text), author.getId());
    }

    @Override
    public void delete(String authorEmail, Long commentId) {
        User author = users.getByEmail(authorEmail);
        VideoComment comment = getOwnedComment(commentId, author.getId());
        comments.delete(comment.id());
    }

    @Override
    public boolean toggleLike(String authorEmail, Long commentId) {
        User author = users.getByEmail(authorEmail);
        if (comments.findById(commentId, author.getId()).isEmpty()) {
            throw new CommentNotFoundException(commentId);
        }
        return comments.toggleLike(commentId, author.getId());
    }

    private VideoComment getOwnedComment(Long commentId, Long authorId) {
        VideoComment comment = comments.findById(commentId, authorId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));
        if (!comment.authorId().equals(authorId)) throw new CommentNotFoundException(commentId);
        return comment;
    }

    private static String validateText(String text) {
        if (text == null || text.isBlank() || text.trim().length() > MAX_TEXT_LENGTH) {
            throw new InvalidCommentException("Comment must contain 1 to " + MAX_TEXT_LENGTH + " characters");
        }
        return text.trim();
    }
}
