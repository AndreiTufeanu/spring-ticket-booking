package com.andreitufeanu.backend.event.dto;

import java.time.Instant;
import java.util.UUID;

public record EventResponseDto(
        UUID id,
        String title,
        String description,
        String location,
        Instant eventDate,
        int totalSeats,
        int availableSeats)
{ }
