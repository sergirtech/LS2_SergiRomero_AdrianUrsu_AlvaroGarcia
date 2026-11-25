package com.tecnocampus.LS2.protube_back.application.service;

import com.tecnocampus.LS2.protube_back.application.port.in.AddCommentUseCase;
import com.tecnocampus.LS2.protube_back.application.port.in.ImportCommentsUseCase;
import com.tecnocampus.LS2.protube_back.application.port.in.ListCommentsUseCase;
import com.tecnocampus.LS2.protube_back.application.port.out.CommentRepository;
import com.tecnocampus.LS2.protube_back.application.port.out.CommentSource;
import com.tecnocampus.LS2.protube_back.application.port.out.UserRepository;
import com.tecnocampus.LS2.protube_back.application.port.out.VideoRepository;
import com.tecnocampus.LS2.protube_back.domain.exception.UserNotFoundException;
import com.tecnocampus.LS2.protube_back.domain.exception.VideoNotFoundException;
import com.tecnocampus.LS2.protube_back.domain.model.Comment;
import com.tecnocampus.LS2.protube_back.domain.model.User;
import com.tecnocampus.LS2.protube_back.domain.model.Video;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CommentService implements ListCommentsUseCase, AddCommentUseCase, ImportCommentsUseCase {

    private final CommentRepository commentRepository;
    private final VideoRepository videoRepository;
    private final UserRepository userRepository;
    private final CommentSource commentSource;

    public CommentService(CommentRepository commentRepository, VideoRepository videoRepository,
                          UserRepository userRepository, CommentSource commentSource) {
        this.commentRepository = commentRepository;
        this.videoRepository = videoRepository;
        this.userRepository = userRepository;
        this.commentSource = commentSource;
    }

    @Override
    public List<Comment> listComments(Long videoId) {
        return commentRepository.findByVideoId(getVideo(videoId).id());
    }

    @Override
    public Comment addComment(String authorEmail, Long videoId, String text) {
        User author = userRepository.findByEmail(User.normalizeEmail(authorEmail))
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        Video video = getVideo(videoId);
        return commentRepository.save(Comment.write(video.id(), author, text));
    }

    @Override
    public int importCommentsFromStore() {
        int imported = 0;
        for (Map.Entry<String, List<CommentSource.StoredComment>> entry : commentSource.readAll().entrySet()) {
            Optional<Video> video = videoRepository.findByFileKey(entry.getKey());
            if (video.isEmpty() || commentRepository.existsImportedByVideoId(video.get().id())) {
                continue;
            }
            Long videoId = video.get().id();
            List<Comment> comments = entry.getValue().stream()
                    .map(stored -> Comment.imported(videoId, stored.author(), stored.text(), stored.createdAt()))
                    .toList();
            commentRepository.saveAll(comments);
            imported += comments.size();
        }
        return imported;
    }

    private Video getVideo(Long videoId) {
        return videoRepository.findById(videoId).orElseThrow(() -> new VideoNotFoundException(videoId));
    }
}
