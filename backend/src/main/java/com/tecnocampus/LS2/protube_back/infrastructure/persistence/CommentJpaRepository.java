package com.tecnocampus.LS2.protube_back.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface CommentJpaRepository extends JpaRepository<CommentJpaEntity, Long> {
    List<CommentJpaEntity> findByVideo_IdOrderByCreatedAtDescIdDesc(Long videoId);

    boolean existsByVideo_IdAndAuthorIdIsNull(Long videoId);
}
