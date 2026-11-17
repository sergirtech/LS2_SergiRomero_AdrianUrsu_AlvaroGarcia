package com.tecnocampus.LS2.protube_back.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface VideoContentJpaRepository extends JpaRepository<VideoContentJpaEntity, Long> {
    Optional<VideoContentJpaEntity> findByVideoId(Long videoId);

    void deleteByVideoId(Long videoId);
}
