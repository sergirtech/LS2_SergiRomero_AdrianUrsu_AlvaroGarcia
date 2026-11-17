package com.tecnocampus.LS2.protube_back.infrastructure.web;

import com.tecnocampus.LS2.protube_back.application.port.in.ManageCommentsUseCase;
import com.tecnocampus.LS2.protube_back.infrastructure.web.dto.CommentRequest;
import com.tecnocampus.LS2.protube_back.infrastructure.web.dto.CommentResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CommentsController {
    private final ManageCommentsUseCase comments;

    public CommentsController(ManageCommentsUseCase comments) {
        this.comments = comments;
    }

    @GetMapping("/api/videos/{videoId}/comments")
    public List<CommentResponse> listForVideo(@PathVariable Long videoId, Authentication authentication) {
        return comments.listForVideo(videoId, authentication == null ? null : authentication.getName())
                .stream().map(CommentResponse::from).toList();
    }

    @GetMapping("/api/comments/mine")
    public List<CommentResponse> listMine(Authentication authentication) {
        return comments.listForAuthor(authentication.getName()).stream().map(CommentResponse::from).toList();
    }

    @PostMapping("/api/videos/{videoId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse create(Authentication authentication, @PathVariable Long videoId,
                                  @Valid @RequestBody CommentRequest request) {
        return CommentResponse.from(comments.create(authentication.getName(), videoId, request.parentId(),
                request.text()));
    }

    @PutMapping("/api/comments/{commentId}")
    public CommentResponse edit(Authentication authentication, @PathVariable Long commentId,
                                @Valid @RequestBody CommentRequest request) {
        return CommentResponse.from(comments.edit(authentication.getName(), commentId, request.text()));
    }

    @DeleteMapping("/api/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Authentication authentication, @PathVariable Long commentId) {
        comments.delete(authentication.getName(), commentId);
    }

    @PostMapping("/api/comments/{commentId}/likes")
    public LikeResponse toggleLike(Authentication authentication, @PathVariable Long commentId) {
        return new LikeResponse(comments.toggleLike(authentication.getName(), commentId));
    }

    public record LikeResponse(boolean liked) {
    }
}
