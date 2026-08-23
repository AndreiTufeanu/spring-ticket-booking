package com.andreitufeanu.backend.booking.controller;

import com.andreitufeanu.backend.booking.dto.BookingDto;
import com.andreitufeanu.backend.booking.dto.CreateBookingDto;
import com.andreitufeanu.backend.booking.service.BookingService;
import com.andreitufeanu.backend.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
@Import(BookingControllerTest.TestSecurityConfig.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @MockitoBean
    private BookingService bookingService;

    @MockitoBean
    private JwtService jwtService;

    private final UUID userId = UUID.randomUUID();
    private final UUID bookingId = UUID.randomUUID();
    private final UUID eventId = UUID.randomUUID();
    private final Instant now = Instant.now();

    @BeforeEach
    void setUp() {
        Authentication auth = new UsernamePasswordAuthenticationToken(userId.toString(), null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void getUserBookings_ShouldReturnList() throws Exception {
        BookingDto dto = new BookingDto(bookingId, eventId, 3, "Concert", now);
        when(bookingService.getUserBookings(userId)).thenReturn(List.of(dto));

        mockMvc.perform(get("/bookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(bookingId.toString()))
                .andExpect(jsonPath("$[0].seatNumber").value(3));
    }

    @Test
    void getBookingById_ShouldReturnBooking() throws Exception {
        BookingDto dto = new BookingDto(bookingId, eventId, 3, "Concert", now);
        when(bookingService.getBookingById(bookingId)).thenReturn(dto);

        mockMvc.perform(get("/bookings/{id}", bookingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId.toString()));
    }

    @Test
    void createBooking_ShouldReturnCreated() throws Exception {
        CreateBookingDto dto = new CreateBookingDto(eventId, 3);
        BookingDto response = new BookingDto(bookingId, eventId, 3, "Concert", now);
        when(bookingService.createBooking(eq(userId), any(CreateBookingDto.class))).thenReturn(response);

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/bookings/" + bookingId))
                .andExpect(jsonPath("$.id").value(bookingId.toString()));
    }

    @Test
    void createBooking_ShouldReturnBadRequest_WhenInvalidData() throws Exception {
        CreateBookingDto invalid = new CreateBookingDto(null, 0);

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cancelBooking_ShouldReturnNoContent() throws Exception {
        doNothing().when(bookingService).cancelBooking(bookingId, userId);

        mockMvc.perform(delete("/bookings/{id}", bookingId))
                .andExpect(status().isNoContent());
    }

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                    .build();
        }
    }
}