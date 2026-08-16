package com.andreitufeanu.backend.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String secretKey,
        String issuer,
        String audience,
        long expirationMinutes,
        int refreshTokenExpiryDays) {
}
