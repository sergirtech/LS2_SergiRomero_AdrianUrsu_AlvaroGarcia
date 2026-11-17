package com.tecnocampus.LS2.protube_back.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

interface CommentLikeJpaRepository extends JpaRepository<CommentLikeJpaEntity, CommentLikeJpaEntity.LikeId> {
    long countByComment_Id(Long commentId);

    boolean existsByComment_IdAndUser_Id(Long commentId, Long userId);

    void deleteByComment_IdAndUser_Id(Long commentId, Long userId);

    void deleteByComment_IdIn(Collection<Long> commentIds);
}
