package com.tecnocampus.LS2.protube_back.infrastructure.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Spring Data repository for video metadata only; binary content lives separately. */
interface VideoJpaRepository extends JpaRepository<VideoJpaEntity, Long> {
    @EntityGraph(attributePaths = "owner")
    List<VideoJpaEntity> findAllByOrderByIdDesc(Pageable pageable);

    @EntityGraph(attributePaths = "owner")
    List<VideoJpaEntity> findByIdLessThanOrderByIdDesc(Long id, Pageable pageable);

    @EntityGraph(attributePaths = "owner")
    List<VideoJpaEntity> findAllByOwner_IdOrderByIdDesc(Long ownerId, Pageable pageable);

    @EntityGraph(attributePaths = "owner")
    List<VideoJpaEntity> findAllByOwner_IdAndIdLessThanOrderByIdDesc(Long ownerId, Long id, Pageable pageable);

    @EntityGraph(attributePaths = "owner")
    Optional<VideoJpaEntity> findWithOwnerById(Long id);
}
