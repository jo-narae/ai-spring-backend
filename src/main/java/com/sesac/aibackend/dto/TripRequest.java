package com.sesac.aibackend.dto;

import com.sesac.aibackend.domain.Trip;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record TripRequest(
        @NotBlank String title,
        @NotBlank String origin,
        @NotBlank String destination
) {

    public Trip toEntity() {
        return Trip.builder().title(title).origin(origin).destination(destination).build();
    }
}
