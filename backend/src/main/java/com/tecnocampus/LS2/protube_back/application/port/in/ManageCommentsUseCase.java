package com.tecnocampus.LS2.protube_back.application.port.in;

import com.tecnocampus.LS2.protube_back.domain.model.VideoComment;

import java.util.List;

public interface ManageCommentsUseCase {
    List<VideoComment> listForVideo(Long videoId, String viewerEmail);

    List<VideoComment> listForAuthor(String authorEmail);

    VideoComment create(String authorEmail, Long videoId, Long parentId, String text);

    VideoComment edit(String authorEmail, Long commentId, String text);

    void delete(String authorEmail, Long commentId);

    boolean toggleLike(String authorEmail, Long commentId);
}
