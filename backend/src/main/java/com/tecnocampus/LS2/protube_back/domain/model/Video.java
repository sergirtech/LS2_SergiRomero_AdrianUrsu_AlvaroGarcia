package com.tecnocampus.LS2.protube_back.domain.model;

import java.time.Instant;

/** Domain video, including its binary content when first uploaded. */
public class Video {
    private final Long id;
    private final String title;
    private final String description;
    private final Long ownerId;
    private final String ownerUsername;
    private final String contentType;
    private final byte[] content;
    private final Instant createdAt;

    public Video(Long id, String title, String description, Long ownerId, String ownerUsername,
                 String contentType, byte[] content, Instant createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.ownerId = ownerId;
        this.ownerUsername = ownerUsername;
        this.contentType = contentType;
        this.content = content;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public String getContentType() {
        return contentType;
    }

    public byte[] getContent() {
        return content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
