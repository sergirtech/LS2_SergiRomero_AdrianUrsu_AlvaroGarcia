package com.tecnocampus.LS2.protube_back.infrastructure.persistence;

import com.tecnocampus.LS2.protube_back.application.port.out.CommentRepository;
import com.tecnocampus.LS2.protube_back.domain.model.Comment;
import org.springframework.stereotype.Component;

import java.util.List;

/** Output adapter: implements the {@link CommentRepository} port with Spring Data JPA. */
@Component
public class CommentPersistenceAdapter implements CommentRepository {
    private final CommentJpaRepository jpaRepository;
    private final VideoJpaRepository videoJpaRepository;

    CommentPersistenceAdapter(CommentJpaRepository jpaRepository, VideoJpaRepository videoJpaRepository) {
        this.jpaRepository = jpaRepository;
        this.videoJpaRepository = videoJpaRepository;
    }

    @Override
    public Comment save(Comment comment) {
        return jpaRepository.save(toEntity(comment)).toDomain(comment.videoId());
    }

    @Override
    public void saveAll(List<Comment> comments) {
        // Spring Data runs saveAll in one transaction.
        jpaRepository.saveAll(comments.stream().map(this::toEntity).toList());
    }

    @Override
    public List<Comment> findByVideoId(Long videoId) {
        return jpaRepository.findByVideo_IdOrderByCreatedAtDescIdDesc(videoId).stream()
                .map(entity -> entity.toDomain(videoId))
                .toList();
    }

    @Override
    public boolean existsImportedByVideoId(Long videoId) {
        return jpaRepository.existsByVideo_IdAndAuthorIdIsNull(videoId);
    }

    private CommentJpaEntity toEntity(Comment comment) {
        // Only the foreign key is needed: a reference avoids loading the video.
        return CommentJpaEntity.fromDomain(comment, videoJpaRepository.getReferenceById(comment.videoId()));
    }
}
