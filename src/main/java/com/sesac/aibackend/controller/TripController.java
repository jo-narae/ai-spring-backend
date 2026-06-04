package com.sesac.aibackend.controller;

import com.sesac.aibackend.domain.Trip;
import com.sesac.aibackend.dto.TripRequest;
import com.sesac.aibackend.dto.TripResponse;
import com.sesac.aibackend.error.NotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/trips")
public class TripController {

    private final Map<Long, Trip> storage = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1);

    @GetMapping
    public List<TripResponse> list() {
        return storage.values().stream()
                .map(TripResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public TripResponse get(@PathVariable Long id) {
        Trip trip = storage.get(id);
        if (trip == null) {
            throw NotFoundException.of("trip", id);
        }
        return TripResponse.from(trip);
    }

    @PostMapping
    public ResponseEntity<TripResponse> create(
            @Valid @RequestBody TripRequest req
    ) {
        long id = sequence.getAndIncrement();
        Trip saved = Trip.builder()
                .id(id)
                .title(req.title())
                .origin(req.origin())
                .destination(req.destination())
                .build();
        storage.put(id, saved);
        return ResponseEntity
                .created(URI.create("/legacy/trips/" + id))
                .body(TripResponse.from(saved));
    }

    @PutMapping("/{id}")
    public TripResponse update(
            @PathVariable Long id,
            @Valid @RequestBody TripRequest req
    ) {
        Trip existing = storage.get(id);
        if (existing == null) {
            throw NotFoundException.of("trip", id);
        }
        existing.setTitle(req.title());
        existing.setOrigin(req.origin());
        existing.setDestination(req.destination());

        return TripResponse.from(existing);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (storage.remove(id) == null) {
            throw NotFoundException.of("trip", id);
        }
        return ResponseEntity.noContent().build();
    }
}