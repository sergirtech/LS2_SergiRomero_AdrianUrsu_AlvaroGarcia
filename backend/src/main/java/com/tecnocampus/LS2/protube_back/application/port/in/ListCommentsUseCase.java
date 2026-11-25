package com.tecnocampus.LS2.protube_back.application.port.in;

import com.tecnocampus.LS2.protube_back.domain.model.Comment;

import java.util.List;

/** Input port: comments of a video, newest first (US-09). */
public interface ListCommentsUseCase {
    List<Comment> listComments(Long videoId);
}
