package com.andreitufeanu.backend.user.service;

import com.andreitufeanu.backend.exceptions.ConflictException;
import com.andreitufeanu.backend.exceptions.UnauthorizedException;
import com.andreitufeanu.backend.security.jwt.JwtService;
import com.andreitufeanu.backend.user.dto.AuthResponseDto;
import com.andreitufeanu.backend.user.dto.LoginDto;
import com.andreitufeanu.backend.user.dto.RegisterDto;
import com.andreitufeanu.backend.user.dto.UserResponseDto;
import com.andreitufeanu.backend.user.entity.User;
import com.andreitufeanu.backend.user.enums.UserRole;
import com.andreitufeanu.backend.user.mapper.UserMapper;
import com.andreitufeanu.backend.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserResponseDto registerUser(@Valid RegisterDto dto) {
        userRepository.findByUsername(dto.username()).ifPresent(user -> {
            throw new ConflictException("Username already exists.");
        });

        User user = userMapper.toEntity(dto);
        user.setRole(UserRole.ROLE_USER);
        user.setPasswordHash(passwordEncoder.encode(dto.password()));

        User createdUser = userRepository.save(user);

        return userMapper.toDto(createdUser);
    }

    public AuthResponseDto loginUser(LoginDto dto) {
        User user = userRepository.findByUsername(dto.username())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials."));

        if (!passwordEncoder.matches(dto.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials.");
        }

        return issueTokens(user);
    }

    public AuthResponseDto refresh(String refreshToken) {
        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token."));

        if (user.getRefreshTokenExpiry() == null || user.getRefreshTokenExpiry().isBefore(Instant.now()))
            throw new UnauthorizedException("Refresh token has expired. Please log in again.");

        return issueTokens(user);
    }

    public void revoke(String refreshToken) {
        userRepository.findByRefreshToken(refreshToken).ifPresent(user -> {
            user.setRefreshToken(null);
            user.setRefreshTokenExpiry(null);
            userRepository.save(user);
        });
    }

    private AuthResponseDto issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken();
        Instant expiry = Instant.now().plus(jwtService.getRefreshTokenExpiryDays(), ChronoUnit.DAYS);

        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiry(expiry);
        userRepository.save(user);

        return new AuthResponseDto(accessToken, refreshToken, jwtService.getRefreshTokenExpiryDays());
    }
}