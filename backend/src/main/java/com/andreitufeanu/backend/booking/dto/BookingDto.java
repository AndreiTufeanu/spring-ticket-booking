package com.andreitufeanu.backend.booking.dto;

import java.time.Instant;
import java.util.UUID;

public record BookingDto(
        UUID id,
        UUID eventId,
        int seatNumber,
        String eventTitle,
        Instant eventDate,
        String location,
        String description)
{ }
