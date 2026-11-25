package com.tecnocampus.LS2.protube_back.infrastructure.web;

import com.tecnocampus.LS2.protube_back.application.port.in.AddCommentUseCase;
import com.tecnocampus.LS2.protube_back.application.port.in.ListCommentsUseCase;
import com.tecnocampus.LS2.protube_back.infrastructure.web.dto.CommentRequest;
import com.tecnocampus.LS2.protube_back.infrastructure.web.dto.CommentResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Comments of a video (US-09). Reading is public; writing needs a session (SecurityConfig). */
@RestController
@RequestMapping("/api/videos/{videoId}/comments")
public class CommentsController {

    private final ListCommentsUseCase listCommentsUseCase;
    private final AddCommentUseCase addCommentUseCase;

    public CommentsController(ListCommentsUseCase listCommentsUseCase, AddCommentUseCase addCommentUseCase) {
        this.listCommentsUseCase = listCommentsUseCase;
        this.addCommentUseCase = addCommentUseCase;
    }

    @GetMapping
    public List<CommentResponse> listComments(@PathVariable Long videoId) {
        return listCommentsUseCase.listComments(videoId).stream().map(CommentResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse addComment(Authentication authentication, @PathVariable Long videoId,
                                      @RequestBody CommentRequest body) {
        return CommentResponse.from(addCommentUseCase.addComment(authentication.getName(), videoId, body.text()));
    }
}
