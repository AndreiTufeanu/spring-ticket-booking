package com.andreitufeanu.backend.event.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record UpdateEventDto(
        @NotBlank(message = "Title is required")
        @Size(max = 50, message = "Title can't have more than {max} characters")
        String title,

        @Size(max = 1000, message = "Description can't have more than {max} characters")
        String description,

        @NotBlank(message = "Location of the event is required")
        @Size(max = 300, message = "Location can't have more than {max} characters")
        String location,

        @NotNull(message = "Date of the event is required")
        Instant eventDate) {
}
