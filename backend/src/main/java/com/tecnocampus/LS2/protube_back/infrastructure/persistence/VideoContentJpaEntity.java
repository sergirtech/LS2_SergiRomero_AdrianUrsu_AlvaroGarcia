package com.tecnocampus.LS2.protube_back.infrastructure.persistence;

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
@Table(name = "video_content")
public class VideoContentJpaEntity {
    @Id
    private Long videoId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "video_id", nullable = false)
    private VideoJpaEntity video;

    @Column(name = "content", nullable = false, length = 104_857_600)
    @JdbcTypeCode(SqlTypes.VARBINARY)
    private byte[] content;

    protected VideoContentJpaEntity() {
    }

    static VideoContentJpaEntity create(VideoJpaEntity video, byte[] content) {
        VideoContentJpaEntity entity = new VideoContentJpaEntity();
        entity.video = video;
        entity.content = content;
        return entity;
    }

    byte[] getContent() {
        return content;
    }

    VideoJpaEntity getVideo() {
        return video;
    }
}
