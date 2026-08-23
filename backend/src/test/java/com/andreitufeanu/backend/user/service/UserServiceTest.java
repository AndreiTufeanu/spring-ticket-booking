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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    private final String rawPassword = "password123";
    private final String encodedPassword = "encoded";
    private final String username = "testuser";
    private final UUID userId = UUID.randomUUID();
    private User user;
    private RegisterDto registerDto;
    private LoginDto loginDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(userId);
        user.setUsername(username);
        user.setPasswordHash(encodedPassword);
        user.setRole(UserRole.ROLE_USER);

        registerDto = new RegisterDto(username, rawPassword);
        loginDto = new LoginDto(username, rawPassword);
    }

    @Test
    void registerUser_Success() {
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(userMapper.toEntity(registerDto)).thenReturn(user);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(new UserResponseDto(userId, username, UserRole.ROLE_USER));

        UserResponseDto result = userService.registerUser(registerDto);

        assertThat(result).isNotNull();
        assertThat(result.username()).isEqualTo(username);
        assertThat(result.role()).isEqualTo(UserRole.ROLE_USER);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getPasswordHash()).isEqualTo(encodedPassword);
        assertThat(savedUser.getRole()).isEqualTo(UserRole.ROLE_USER);
    }

    @Test
    void registerUser_ThrowsConflict_WhenUsernameExists() {
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.registerUser(registerDto))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Username already exists.");

        verify(userRepository, never()).save(any());
    }

    @Test
    void loginUser_Success() {
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);
        when(jwtService.generateAccessToken(user)).thenReturn("access");
        when(jwtService.generateRefreshToken()).thenReturn("refresh");
        when(jwtService.getRefreshTokenExpiryDays()).thenReturn(7);
        when(userRepository.save(any(User.class))).thenReturn(user);

        AuthResponseDto result = userService.loginUser(loginDto);

        assertThat(result).isNotNull();
        assertThat(result.accessToken()).isEqualTo("access");
        assertThat(result.refreshToken()).isEqualTo("refresh");
        assertThat(result.refreshTokenExpiryDays()).isEqualTo(7);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getRefreshToken()).isEqualTo("refresh");
        assertThat(savedUser.getRefreshTokenExpiry()).isNotNull();
    }

    @Test
    void loginUser_ThrowsUnauthorized_WhenUserNotFound() {
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.loginUser(loginDto))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid credentials.");

        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void loginUser_ThrowsUnauthorized_WhenPasswordMismatch() {
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(false);

        assertThatThrownBy(() -> userService.loginUser(loginDto))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid credentials.");
    }

    @Test
    void refresh_Success() {
        String refreshToken = "validRefresh";
        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiry(Instant.now().plus(7, ChronoUnit.DAYS));

        when(userRepository.findByRefreshToken(refreshToken)).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(user)).thenReturn("newAccess");
        when(jwtService.generateRefreshToken()).thenReturn("newRefresh");
        when(jwtService.getRefreshTokenExpiryDays()).thenReturn(7);
        when(userRepository.save(any(User.class))).thenReturn(user);

        AuthResponseDto result = userService.refresh(refreshToken);

        assertThat(result.accessToken()).isEqualTo("newAccess");
        assertThat(result.refreshToken()).isEqualTo("newRefresh");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getRefreshToken()).isEqualTo("newRefresh");
    }

    @Test
    void refresh_ThrowsUnauthorized_WhenRefreshTokenNotFound() {
        when(userRepository.findByRefreshToken("invalid")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.refresh("invalid"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid refresh token.");
    }

    @Test
    void refresh_ThrowsUnauthorized_WhenRefreshTokenExpired() {
        String refreshToken = "expired";
        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiry(Instant.now().minus(1, ChronoUnit.DAYS));

        when(userRepository.findByRefreshToken(refreshToken)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.refresh(refreshToken))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Refresh token has expired. Please log in again.");
    }

    @Test
    void revoke_Success() {
        String refreshToken = "toRevoke";
        user.setRefreshToken(refreshToken);

        when(userRepository.findByRefreshToken(refreshToken)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.revoke(refreshToken);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getRefreshToken()).isNull();
        assertThat(savedUser.getRefreshTokenExpiry()).isNull();
    }

    @Test
    void revoke_DoesNothing_WhenTokenNotFound() {
        String refreshToken = "unknown";
        when(userRepository.findByRefreshToken(refreshToken)).thenReturn(Optional.empty());

        userService.revoke(refreshToken);

        verify(userRepository, never()).save(any());
    }
}
