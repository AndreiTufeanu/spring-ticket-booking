package com.andreitufeanu.backend.user.controller;

import com.andreitufeanu.backend.user.dto.AuthResponseDto;
import com.andreitufeanu.backend.user.dto.LoginDto;
import com.andreitufeanu.backend.user.dto.RegisterDto;
import com.andreitufeanu.backend.user.dto.UserResponseDto;
import com.andreitufeanu.backend.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";

    private final UserService userService;

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
        return ResponseEntity.ok(result.accessToken());
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

}
