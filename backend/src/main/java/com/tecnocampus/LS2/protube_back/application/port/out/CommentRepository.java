package com.tecnocampus.LS2.protube_back.application.port.out;

import com.tecnocampus.LS2.protube_back.domain.model.VideoComment;

import java.util.List;
import java.util.Optional;

public interface CommentRepository {
    List<VideoComment> findByVideoId(Long videoId, Long viewerId);

    List<VideoComment> findByAuthorId(Long authorId);

    Optional<VideoComment> findById(Long commentId, Long viewerId);

    VideoComment save(Long authorId, Long videoId, Long parentId, String text);

    VideoComment update(Long commentId, String text, Long viewerId);

    void delete(Long commentId);

    void deleteByVideoId(Long videoId);

    boolean toggleLike(Long commentId, Long userId);

    boolean videoExists(Long videoId);
}
