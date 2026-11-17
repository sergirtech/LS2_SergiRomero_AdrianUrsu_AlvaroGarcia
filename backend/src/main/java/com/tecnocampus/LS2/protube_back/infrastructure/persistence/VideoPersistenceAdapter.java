package com.tecnocampus.LS2.protube_back.infrastructure.persistence;

import com.tecnocampus.LS2.protube_back.application.port.out.VideoRepository;
import com.tecnocampus.LS2.protube_back.domain.model.PageResult;
import com.tecnocampus.LS2.protube_back.domain.model.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Output adapter: implements the {@link VideoRepository} port with Spring Data JPA. */
@Component
public class VideoPersistenceAdapter implements VideoRepository {
    private final VideoJpaRepository jpaRepository;

    VideoPersistenceAdapter(VideoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Video save(Video video) {
        return jpaRepository.save(VideoJpaEntity.fromDomain(video)).toDomain();
    }

    @Override
    public Optional<Video> findById(Long id) {
        return jpaRepository.findById(id).map(VideoJpaEntity::toDomain);
    }

    @Override
    public PageResult<Video> findPage(int page, int size) {
        Page<VideoJpaEntity> result = jpaRepository.findAll(PageRequest.of(page, size, Sort.by("id")));
        return new PageResult<>(result.map(VideoJpaEntity::toDomain).getContent(), page, size,
                result.getTotalElements());
    }

    @Override
    public PageResult<Video> findPageByOwner(Long ownerId, int page, int size) {
        Page<VideoJpaEntity> result = jpaRepository.findByOwnerId(ownerId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")));
        return new PageResult<>(result.map(VideoJpaEntity::toDomain).getContent(), page, size,
                result.getTotalElements());
    }

    @Override
    public boolean existsByFileKey(String fileKey) {
        return jpaRepository.existsByFileKey(fileKey);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}
