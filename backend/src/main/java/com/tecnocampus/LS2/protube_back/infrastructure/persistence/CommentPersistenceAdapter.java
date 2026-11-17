package com.tecnocampus.LS2.protube_back.infrastructure.persistence;

import com.tecnocampus.LS2.protube_back.application.port.out.CommentRepository;
import com.tecnocampus.LS2.protube_back.domain.exception.CommentNotFoundException;
import com.tecnocampus.LS2.protube_back.domain.exception.UserNotFoundException;
import com.tecnocampus.LS2.protube_back.domain.exception.VideoNotFoundException;
import com.tecnocampus.LS2.protube_back.domain.model.VideoComment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
class CommentPersistenceAdapter implements CommentRepository {
    private final CommentJpaRepository comments;
    private final CommentLikeJpaRepository likes;
    private final UserJpaRepository users;
    private final VideoJpaRepository videos;

    CommentPersistenceAdapter(CommentJpaRepository comments, CommentLikeJpaRepository likes,
                              UserJpaRepository users, VideoJpaRepository videos) {
        this.comments = comments;
        this.likes = likes;
        this.users = users;
        this.videos = videos;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoComment> findByVideoId(Long videoId, Long viewerId) {
        return comments.findAllByVideo_IdOrderByCreatedAtDesc(videoId).stream()
                .map(comment -> toDomain(comment, viewerId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoComment> findByAuthorId(Long authorId) {
        return comments.findAllByAuthor_IdOrderByCreatedAtDesc(authorId).stream()
                .map(comment -> toDomain(comment, authorId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VideoComment> findById(Long commentId, Long viewerId) {
        return comments.findWithDetailsById(commentId).map(comment -> toDomain(comment, viewerId));
    }

    @Override
    @Transactional
    public VideoComment save(Long authorId, Long videoId, Long parentId, String text) {
        UserJpaEntity author = users.findById(authorId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        VideoJpaEntity video = videos.findWithOwnerById(videoId)
                .orElseThrow(() -> new VideoNotFoundException(videoId));
        CommentJpaEntity parent = parentId == null ? null : comments.findWithDetailsById(parentId)
                .orElseThrow(() -> new CommentNotFoundException(parentId));
        CommentJpaEntity saved = comments.save(CommentJpaEntity.create(video, author, parent, text));
        return comments.findWithDetailsById(saved.getId()).orElseThrow().toDomain(0, false);
    }

    @Override
    @Transactional
    public VideoComment update(Long commentId, String text, Long viewerId) {
        CommentJpaEntity comment = comments.findWithDetailsById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));
        comment.updateText(text);
        return toDomain(comments.save(comment), viewerId);
    }

    @Override
    @Transactional
    public void delete(Long commentId) {
        CommentJpaEntity comment = comments.findWithDetailsById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));
        comments.delete(comment);
    }

    @Override
    @Transactional
    public void deleteByVideoId(Long videoId) {
        comments.deleteAll(comments.findAllByVideo_IdAndParentIsNull(videoId));
    }

    @Override
    @Transactional
    public boolean toggleLike(Long commentId, Long userId) {
        CommentJpaEntity comment = comments.findWithDetailsById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));
        UserJpaEntity user = users.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        if (likes.existsByComment_IdAndUser_Id(commentId, userId)) {
            likes.deleteByComment_IdAndUser_Id(commentId, userId);
            return false;
        }
        likes.save(new CommentLikeJpaEntity(comment, user));
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean videoExists(Long videoId) {
        return videos.existsById(videoId);
    }

    private VideoComment toDomain(CommentJpaEntity comment, Long viewerId) {
        long count = likes.countByComment_Id(comment.getId());
        boolean liked = viewerId != null && likes.existsByComment_IdAndUser_Id(comment.getId(), viewerId);
        return comment.toDomain(count, liked);
    }
}
