package com.tecnocampus.LS2.protube_back.infrastructure.web.dto;

import java.util.List;

/** Body of PUT /api/videos/{id} (US-07). The rules are checked in the domain (VideoMetadata), which answers 400. */
public record VideoUpdateRequest(String title, String description, List<String> tags, String category) {
}
