package com.tecnocampus.LS2.protube_back.infrastructure.persistence;

import com.tecnocampus.LS2.protube_back.domain.model.VideoComment;
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
@Table(name = "video_comment")
class CommentJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "video_id", nullable = false)
    private VideoJpaEntity video;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private UserJpaEntity author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private CommentJpaEntity parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<CommentJpaEntity> replies = new ArrayList<>();

    @OneToMany(mappedBy = "comment", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<CommentLikeJpaEntity> likes = new ArrayList<>();

    @Column(nullable = false, length = 1000)
    private String text;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected CommentJpaEntity() {
    }

    static CommentJpaEntity create(VideoJpaEntity video, UserJpaEntity author, CommentJpaEntity parent,
                                   String text) {
        CommentJpaEntity entity = new CommentJpaEntity();
        entity.video = video;
        entity.author = author;
        entity.parent = parent;
        entity.text = text;
        entity.createdAt = Instant.now();
        return entity;
    }

    void updateText(String text) {
        this.text = text;
    }

    VideoComment toDomain(long likes, boolean liked) {
        return new VideoComment(id, video.getId(), video.getTitle(), author.getId(), author.getUsername(),
                parent == null ? null : parent.getId(), text, createdAt, likes, liked);
    }

    Long getId() {
        return id;
    }

    Long getAuthorId() {
        return author.getId();
    }

    Long getVideoId() {
        return video.getId();
    }

    Long getParentId() {
        return parent == null ? null : parent.getId();
    }
}
