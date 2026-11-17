package com.tecnocampus.LS2.protube_back.infrastructure.persistence;

import com.tecnocampus.LS2.protube_back.domain.model.Video;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/** JPA mapping of the domain {@link Video}. */
@Entity
@Table(name = "video")
public class VideoJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String fileKey;

    @Column(nullable = false)
    private String title;

    private String author;

    @Column(length = Video.MAX_DESCRIPTION_LENGTH)
    private String description;

    private double durationSeconds;
    private int width;
    private int height;
    private long viewCount;
    private long likeCount;
    private Instant publishedAt;

    protected VideoJpaEntity() {
        // required by JPA
    }

    static VideoJpaEntity fromDomain(Video video) {
        VideoJpaEntity entity = new VideoJpaEntity();
        entity.id = video.id();
        entity.fileKey = video.fileKey();
        entity.title = video.title();
        entity.author = video.author();
        entity.description = video.description();
        entity.durationSeconds = video.durationSeconds();
        entity.width = video.width();
        entity.height = video.height();
        entity.viewCount = video.viewCount();
        entity.likeCount = video.likeCount();
        entity.publishedAt = video.publishedAt();
        return entity;
    }

    Video toDomain() {
        return new Video(id, fileKey, title, author, description, durationSeconds, width, height,
                viewCount, likeCount, publishedAt);
    }
}
