package com.andreitufeanu.backend.event.dto;

import jakarta.validation.constraints.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CreateEventDto(
        @NotBlank(message = "Title is required")
        @Size(max = 50, message = "Title can't have more than {max} characters")
        String title,

        @Size(max = 1000, message = "Description can't have more than {max} characters")
        String description,

        @NotBlank(message = "Location of the event is required")
        @Size(max = 300, message = "Location can't have more than {max} characters")
        String location,

        @NotNull(message = "Date of the event is required")
        Instant eventDate,

        @Min(value = 1, message = "Total seats must be at least {value}")
        @Max(value = 1_000_000, message = "Total seats can't exceed {value}")
        int totalSeats,

        List<UUID> categoryIds)
{ }
