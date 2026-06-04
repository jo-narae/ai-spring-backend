package com.sesac.aibackend.dto;

import com.sesac.aibackend.domain.Trip;
import jakarta.validation.constraints.NotBlank;

public record TripResponse(Long id, String title, String origin, String destination) {

    public static TripResponse from(Trip trip) {
        return new TripResponse(trip.getId(), trip.getTitle(), trip.getOrigin(), trip.getDestination());
    }
}
