package com.andreitufeanu.backend.user.controller;

import com.andreitufeanu.backend.exceptions.UnauthorizedException;
import com.andreitufeanu.backend.user.dto.AuthResponseDto;
import com.andreitufeanu.backend.user.dto.LoginDto;
import com.andreitufeanu.backend.user.dto.RegisterDto;
import com.andreitufeanu.backend.user.dto.UserResponseDto;
import com.andreitufeanu.backend.user.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";

    private final UserService userService;
    private final ObjectMapper objectMapper;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> registerUser(@RequestBody RegisterDto user) {
        return ResponseEntity.ok().body(userService.registerUser(user));
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody LoginDto loginDto,
                                                     HttpServletRequest request,
                                                     HttpServletResponse response) {
        AuthResponseDto result = userService.loginUser(loginDto);
        setRefreshTokenCookie(request, response, result.refreshToken(), result.refreshTokenExpiryDays());
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(result.accessToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<String> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshToken(request);
        if (refreshToken == null || refreshToken.isEmpty())
            throw new UnauthorizedException("Missing refresh token.");

        AuthResponseDto result = userService.refresh(refreshToken);
        setRefreshTokenCookie(request, response, result.refreshToken(), result.refreshTokenExpiryDays());
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(result.accessToken()));
    }

    @PostMapping("/revoke")
    public ResponseEntity<Void> revoke(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshToken(request);
        if (refreshToken != null && !refreshToken.isEmpty())
            userService.revoke(refreshToken);

        deleteRefreshTokenCookie(request, response);
        return ResponseEntity.noContent().build();
    }

    private String extractRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if (REFRESH_TOKEN_COOKIE.equals(cookie.getName())) return cookie.getValue();
        }
        return null;
    }

    private void setRefreshTokenCookie(HttpServletRequest request, HttpServletResponse response,
                                       String refreshToken, int expiryDays) {
        boolean isLocal = "localhost".equalsIgnoreCase(request.getServerName());

        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, refreshToken)
                .httpOnly(true)
                .secure(!isLocal)
                .sameSite("Strict")
                .path("/")
                .maxAge(Duration.ofDays(expiryDays))
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    private void deleteRefreshTokenCookie(HttpServletRequest request, HttpServletResponse response) {
        boolean isLocal = "localhost".equalsIgnoreCase(request.getServerName());

        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
                .httpOnly(true)
                .secure(!isLocal)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

}
