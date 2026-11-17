package com.tecnocampus.LS2.protube_back.infrastructure.persistence;

import com.tecnocampus.LS2.protube_back.domain.model.Video;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** JPA mapping of the domain {@link Video}. */
@Entity
@Table(name = "video", indexes = @Index(name = "idx_video_owner", columnList = "ownerId"))
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

    /** app_user id of the uploader; null for videos imported from the videoGrabber. */
    private Long ownerId;

    // A handful of short tags per video: loaded with the video, deleted with it.
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "video_tag", joinColumns = @JoinColumn(name = "video_id"))
    @OrderColumn(name = "position")
    @Column(name = "tag", length = Video.MAX_TAG_LENGTH, nullable = false)
    private List<String> tags = new ArrayList<>();

    @Column(length = Video.MAX_CATEGORY_LENGTH)
    private String category;

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
        entity.ownerId = video.ownerId();
        entity.tags = new ArrayList<>(video.tags());
        entity.category = video.category();
        return entity;
    }

    Video toDomain() {
        return new Video(id, fileKey, title, author, description, durationSeconds, width, height,
                viewCount, likeCount, publishedAt, ownerId, tags, category);
    }
}
