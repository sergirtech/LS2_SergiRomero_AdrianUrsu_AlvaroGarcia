package com.tecnocampus.LS2.protube_back.domain.model;

import java.time.Instant;

/** Video metadata without its potentially large binary content. */
public record VideoSummary(Long id, String title, String description, Long ownerId, String ownerUsername,
                           Instant createdAt) {
}
