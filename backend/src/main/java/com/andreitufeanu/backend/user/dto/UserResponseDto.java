package com.andreitufeanu.backend.user.dto;

import com.andreitufeanu.backend.user.enums.UserRole;

import java.util.UUID;

public record UserResponseDto(
        UUID id,
        String username,
        UserRole role) {
}
