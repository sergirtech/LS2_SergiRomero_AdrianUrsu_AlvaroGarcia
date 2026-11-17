package com.tecnocampus.LS2.protube_back.infrastructure.persistence;

import com.tecnocampus.LS2.protube_back.domain.model.Playlist;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "playlist")
class PlaylistJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private UserJpaEntity owner;

    @Column(nullable = false, length = 80)
    private String name;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "playlist_video",
            joinColumns = @JoinColumn(name = "playlist_id"),
            inverseJoinColumns = @JoinColumn(name = "video_id"))
    @OrderColumn(name = "position")
    private List<VideoJpaEntity> videos = new ArrayList<>();

    protected PlaylistJpaEntity() {
    }

    static PlaylistJpaEntity create(UserJpaEntity owner, String name) {
        PlaylistJpaEntity entity = new PlaylistJpaEntity();
        entity.owner = owner;
        entity.name = name;
        return entity;
    }

    void rename(String name) {
        this.name = name;
    }

    void addVideo(VideoJpaEntity video) {
        if (videos.stream().noneMatch(existing -> existing.getId().equals(video.getId()))) videos.add(video);
    }

    void removeVideo(Long videoId) {
        videos.removeIf(video -> video.getId().equals(videoId));
    }

    Playlist toDomain() {
        return new Playlist(id, owner.getId(), name, videos.stream().map(VideoJpaEntity::getId).toList());
    }

    Long getId() {
        return id;
    }

    Long getOwnerId() {
        return owner.getId();
    }
}
