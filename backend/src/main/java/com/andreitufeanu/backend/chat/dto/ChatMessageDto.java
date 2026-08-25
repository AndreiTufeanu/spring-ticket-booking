package com.andreitufeanu.backend.chat.dto;

import java.time.Instant;
import java.util.UUID;

public record ChatMessageDto(
        UUID id,
        String role,
        String content,
        Instant createdAt
) {}
