package com.tecnocampus.LS2.protube_back.infrastructure.persistence;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface PlaylistJpaRepository extends JpaRepository<PlaylistJpaEntity, Long> {
    @EntityGraph(attributePaths = {"owner", "videos"})
    List<PlaylistJpaEntity> findAllByOwner_IdOrderByIdDesc(Long ownerId);

    @EntityGraph(attributePaths = {"owner", "videos"})
    Optional<PlaylistJpaEntity> findWithVideosById(Long id);

}
