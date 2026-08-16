package com.andreitufeanu.backend.user.dto;

public record AuthResponseDto(
        String accessToken,
        String refreshToken,
        int refreshTokenExpiryDays) {
}
