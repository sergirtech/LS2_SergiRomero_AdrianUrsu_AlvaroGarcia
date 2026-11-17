package com.tecnocampus.LS2.protube_back.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentRequest(@NotBlank @Size(max = 1000) String text, Long parentId) {
}
