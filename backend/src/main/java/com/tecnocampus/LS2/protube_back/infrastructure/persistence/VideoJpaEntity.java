package com.tecnocampus.LS2.protube_back.infrastructure.persistence;

import com.tecnocampus.LS2.protube_back.domain.model.Video;
import com.tecnocampus.LS2.protube_back.domain.model.VideoSummary;
import jakarta.persistence.Column;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "video")
public class VideoJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 2_000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private UserJpaEntity owner;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "video", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<CommentJpaEntity> comments = new ArrayList<>();

    protected VideoJpaEntity() {
    }

    static VideoJpaEntity fromDomain(Video video, UserJpaEntity owner) {
        VideoJpaEntity entity = new VideoJpaEntity();
        entity.title = video.getTitle();
        entity.description = video.getDescription();
        entity.owner = owner;
        entity.contentType = video.getContentType();
        entity.createdAt = video.getCreatedAt();
        return entity;
    }

    void updateMetadata(String title, String description) {
        this.title = title;
        this.description = description;
    }

    VideoSummary toSummary() {
        return new VideoSummary(id, title, description, owner.getId(), owner.getUsername(), createdAt);
    }

    Long getId() {
        return id;
    }

    String getTitle() {
        return title;
    }

    String getContentType() {
        return contentType;
    }
}
