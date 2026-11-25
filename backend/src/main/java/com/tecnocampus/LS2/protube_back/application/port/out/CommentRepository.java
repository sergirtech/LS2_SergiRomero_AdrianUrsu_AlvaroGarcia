package com.tecnocampus.LS2.protube_back.application.port.out;

import com.tecnocampus.LS2.protube_back.domain.model.Comment;

import java.util.List;

/** Output port: comment persistence. */
public interface CommentRepository {
    Comment save(Comment comment);

    /** All or nothing, so an interrupted import is repeated completely on the next startup. */
    void saveAll(List<Comment> comments);

    /** Newest first. */
    List<Comment> findByVideoId(Long videoId);

    /** Whether the video has comments without a ProTube author, i.e. imported from the store. */
    boolean existsImportedByVideoId(Long videoId);
}
