package com.tecnocampus.LS2.protube_back.infrastructure.persistence;

import com.tecnocampus.LS2.protube_back.domain.model.Comment;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

/** JPA mapping of the domain {@link Comment}. */
@Entity
@Table(name = "video_comment", indexes = @Index(name = "idx_video_comment_video", columnList = "video_id"))
public class CommentJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The database deletes the comments together with their video (ON DELETE CASCADE).
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "video_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private VideoJpaEntity video;

    /** app_user id of the author; null for comments imported from the videoGrabber. */
    private Long authorId;

    @Column(nullable = false)
    private String authorName;

    @Column(nullable = false, length = Comment.MAX_TEXT_LENGTH)
    private String text;

    @Column(nullable = false)
    private Instant createdAt;

    protected CommentJpaEntity() {
        // required by JPA
    }

    static CommentJpaEntity fromDomain(Comment comment, VideoJpaEntity video) {
        CommentJpaEntity entity = new CommentJpaEntity();
        entity.id = comment.id();
        entity.video = video;
        entity.authorId = comment.authorId();
        entity.authorName = comment.authorName();
        entity.text = comment.text();
        entity.createdAt = comment.createdAt();
        return entity;
    }

    Comment toDomain(Long videoId) {
        return new Comment(id, videoId, authorId, authorName, text, createdAt);
    }
}
