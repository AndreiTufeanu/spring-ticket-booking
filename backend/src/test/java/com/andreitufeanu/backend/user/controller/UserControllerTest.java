package com.andreitufeanu.backend.user.controller;

import com.andreitufeanu.backend.exceptions.UnauthorizedException;
import com.andreitufeanu.backend.security.jwt.JwtService;
import com.andreitufeanu.backend.user.dto.AuthResponseDto;
import com.andreitufeanu.backend.user.dto.LoginDto;
import com.andreitufeanu.backend.user.dto.RegisterDto;
import com.andreitufeanu.backend.user.dto.UserResponseDto;
import com.andreitufeanu.backend.user.enums.UserRole;
import com.andreitufeanu.backend.user.service.UserService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(UserControllerTest.TestSecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    private final UUID userId = UUID.randomUUID();
    private final String username = "testuser";
    private final String password = "password123";

    @Test
    void registerUser_ShouldReturnCreatedUser() throws Exception {
        RegisterDto registerDto = new RegisterDto(username, password);
        UserResponseDto responseDto = new UserResponseDto(userId, username, UserRole.ROLE_USER);

        when(userService.registerUser(any(RegisterDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(registerDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.role").value("ROLE_USER"));
    }

    @Test
    void registerUser_ShouldReturnBadRequest_WhenInvalidData() throws Exception {
        RegisterDto invalid = new RegisterDto("", "");

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginUser_ShouldSetCookieAndReturnAccessToken() throws Exception {
        LoginDto loginDto = new LoginDto(username, password);
        AuthResponseDto authResponse = new AuthResponseDto("accessToken123", "refreshToken456", 7);

        when(userService.loginUser(any(LoginDto.class))).thenReturn(authResponse);

        MvcResult result = mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(cookie().httpOnly("refreshToken", true))
                .andExpect(cookie().path("refreshToken", "/"))
                .andExpect(cookie().maxAge("refreshToken", 7 * 24 * 60 * 60))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).isEqualTo("\"accessToken123\"");
    }

    @Test
    void loginUser_ShouldReturnUnauthorized_WhenInvalidCredentials() throws Exception {
        LoginDto loginDto = new LoginDto(username, password);
        when(userService.loginUser(any(LoginDto.class)))
                .thenThrow(new UnauthorizedException("Invalid credentials."));

        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(loginDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_ShouldReturnNewTokensAndSetCookie() throws Exception {
        String refreshToken = "validRefresh";
        AuthResponseDto authResponse = new AuthResponseDto("newAccess", "newRefresh", 7);

        when(userService.refresh(eq(refreshToken))).thenReturn(authResponse);

        MvcResult result = mockMvc.perform(post("/users/refresh")
                        .cookie(new Cookie("refreshToken", refreshToken)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(cookie().httpOnly("refreshToken", true))
                .andExpect(cookie().maxAge("refreshToken", 7 * 24 * 60 * 60))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).isEqualTo("\"newAccess\"");
    }

    @Test
    void refresh_ShouldReturnUnauthorized_WhenMissingCookie() throws Exception {
        mockMvc.perform(post("/users/refresh"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_ShouldReturnUnauthorized_WhenInvalidToken() throws Exception {
        String invalidToken = "invalid";
        when(userService.refresh(eq(invalidToken)))
                .thenThrow(new UnauthorizedException("Invalid refresh token."));

        mockMvc.perform(post("/users/refresh")
                        .cookie(new Cookie("refreshToken", invalidToken)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void revoke_ShouldClearCookieAndReturnNoContent() throws Exception {
        String refreshToken = "toRevoke";
        doNothing().when(userService).revoke(refreshToken);

        mockMvc.perform(post("/users/revoke")
                        .cookie(new Cookie("refreshToken", refreshToken)))
                .andExpect(status().isNoContent())
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(cookie().maxAge("refreshToken", 0));
    }

    @Test
    void revoke_ShouldReturnNoContent_EvenIfNoCookie() throws Exception {
        mockMvc.perform(post("/users/revoke"))
                .andExpect(status().isNoContent())
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(cookie().maxAge("refreshToken", 0));
    }

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                    .build();
        }
    }
}