package com.tecnocampus.LS2.protube_back.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PlaylistNameRequest(@NotBlank @Size(max = 80) String name) {
}
