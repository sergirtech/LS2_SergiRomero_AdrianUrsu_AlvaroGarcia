package com.tecnocampus.LS2.protube_back.infrastructure.persistence;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface CommentJpaRepository extends JpaRepository<CommentJpaEntity, Long> {
    @EntityGraph(attributePaths = {"video", "author", "parent"})
    List<CommentJpaEntity> findAllByVideo_IdOrderByCreatedAtDesc(Long videoId);

    @EntityGraph(attributePaths = {"video", "author", "parent"})
    List<CommentJpaEntity> findAllByAuthor_IdOrderByCreatedAtDesc(Long authorId);

    @EntityGraph(attributePaths = {"video", "author", "parent"})
    Optional<CommentJpaEntity> findWithDetailsById(Long id);

    @EntityGraph(attributePaths = {"video", "author", "parent"})
    List<CommentJpaEntity> findAllByVideo_IdAndParentIsNull(Long videoId);
}
