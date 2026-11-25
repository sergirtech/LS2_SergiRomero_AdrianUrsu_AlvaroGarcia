package com.tecnocampus.LS2.protube_back.application.port.in;

import com.tecnocampus.LS2.protube_back.domain.model.Comment;

/** Input port: a registered user comments on a video (US-09). */
public interface AddCommentUseCase {
    Comment addComment(String authorEmail, Long videoId, String text);
}
