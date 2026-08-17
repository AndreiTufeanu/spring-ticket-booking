package com.andreitufeanu.backend.exceptions.handler;

import java.time.Instant;

public record ApiError(
        Instant timestamp,
        int status,
        String message
) {}