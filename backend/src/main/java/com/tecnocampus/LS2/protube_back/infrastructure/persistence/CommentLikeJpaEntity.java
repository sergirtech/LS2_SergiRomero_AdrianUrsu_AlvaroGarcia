package com.tecnocampus.LS2.protube_back.infrastructure.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "comment_like")
@IdClass(CommentLikeJpaEntity.LikeId.class)
class CommentLikeJpaEntity {
    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comment_id", nullable = false)
    private CommentJpaEntity comment;

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserJpaEntity user;

    protected CommentLikeJpaEntity() {
    }

    CommentLikeJpaEntity(CommentJpaEntity comment, UserJpaEntity user) {
        this.comment = comment;
        this.user = user;
    }

    static class LikeId implements Serializable {
        private Long comment;
        private Long user;

        public LikeId() {
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof LikeId likeId)) return false;
            return Objects.equals(comment, likeId.comment) && Objects.equals(user, likeId.user);
        }

        @Override
        public int hashCode() {
            return Objects.hash(comment, user);
        }
    }
}
