package com.andreitufeanu.backend.booking.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateBookingDto(
        @NotNull(message = "Event id is required")
        UUID eventId,

        @Min(value = 1, message = "Seat number must be at least {value}")
        @Max(value = 1_000_000, message = "Seat number can't exceed {value}")
        int seatNumber)
{ }
