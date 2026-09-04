package com.andreitufeanu.backend.booking.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateBookingDto(
        @NotNull(message = "Event id is required")
        UUID eventId)
{ }