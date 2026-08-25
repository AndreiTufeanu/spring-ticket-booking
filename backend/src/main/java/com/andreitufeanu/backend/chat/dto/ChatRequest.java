package com.andreitufeanu.backend.chat.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
        @NotBlank(message = "Message must not be empty")
        String message
) {}