package com.tecnocampus.LS2.protube_back.infrastructure.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VideoUpdateRequest(
        @NotNull @Size(min = 1, max = 150) String title,
        @NotNull @Size(max = 2_000) String description) {
}
