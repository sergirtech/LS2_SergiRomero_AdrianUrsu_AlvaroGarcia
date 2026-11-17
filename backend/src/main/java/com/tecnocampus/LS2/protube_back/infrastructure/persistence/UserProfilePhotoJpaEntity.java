package com.tecnocampus.LS2.protube_back.infrastructure.persistence;

import com.tecnocampus.LS2.protube_back.domain.model.ProfilePhoto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "user_profile_photo")
class UserProfilePhotoJpaEntity {
    @Id
    private Long userId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserJpaEntity user;

    @Column(name = "content_type", nullable = false, length = 32)
    private String contentType;

    @Column(name = "content", nullable = false, length = 5_242_880)
    @JdbcTypeCode(SqlTypes.VARBINARY)
    private byte[] content;

    protected UserProfilePhotoJpaEntity() {
    }

    static UserProfilePhotoJpaEntity create(UserJpaEntity user, ProfilePhoto photo) {
        UserProfilePhotoJpaEntity entity = new UserProfilePhotoJpaEntity();
        entity.user = user;
        entity.contentType = photo.contentType();
        entity.content = photo.content();
        return entity;
    }

    void update(ProfilePhoto photo) {
        contentType = photo.contentType();
        content = photo.content();
    }

    ProfilePhoto toDomain() {
        return new ProfilePhoto(contentType, content);
    }
}
