package com.sesac.aibackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChatLogRequest(
        @NotNull Long userId,
        @NotBlank String prompt,
        String response
) {
}
