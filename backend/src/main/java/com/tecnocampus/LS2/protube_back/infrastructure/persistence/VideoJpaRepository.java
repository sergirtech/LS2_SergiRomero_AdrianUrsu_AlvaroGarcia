package com.tecnocampus.LS2.protube_back.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoJpaRepository extends JpaRepository<VideoJpaEntity, Long> {
    boolean existsByFileKey(String fileKey);

    Page<VideoJpaEntity> findByOwnerId(Long ownerId, Pageable pageable);
}
