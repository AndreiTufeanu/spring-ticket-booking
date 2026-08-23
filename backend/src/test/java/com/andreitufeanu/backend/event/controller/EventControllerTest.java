package com.andreitufeanu.backend.event.controller;

import com.andreitufeanu.backend.event.dto.CreateEventDto;
import com.andreitufeanu.backend.event.dto.EventResponseDto;
import com.andreitufeanu.backend.event.dto.UpdateEventDto;
import com.andreitufeanu.backend.event.service.EventService;
import com.andreitufeanu.backend.security.jwt.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
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

@WebMvcTest(EventController.class)
@Import(EventControllerTest.TestSecurityConfig.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private EventService eventService;

    @MockitoBean
    private JwtService jwtService;

    private final UUID eventId = UUID.randomUUID();
    private final Instant now = Instant.now();

    @Test
    void getAllEvents_ShouldReturnList() throws Exception {
        EventResponseDto dto = new EventResponseDto(eventId, "Event", "Desc", "Loc", now, 100, 80);
        when(eventService.getUpcomingEvents()).thenReturn(List.of(dto));

        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(eventId.toString()))
                .andExpect(jsonPath("$[0].title").value("Event"));
    }

    @Test
    void getEventById_ShouldReturnEvent() throws Exception {
        EventResponseDto dto = new EventResponseDto(eventId, "Event", "Desc", "Loc", now, 100, 80);
        when(eventService.getEventById(eventId)).thenReturn(dto);

        mockMvc.perform(get("/events/{id}", eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventId.toString()));
    }

    @Test
    void getEventById_ShouldReturn500_WhenServiceThrowsRuntimeException() throws Exception {
        when(eventService.getEventById(eventId)).thenThrow(new RuntimeException("Event not found"));

        mockMvc.perform(get("/events/{id}", eventId))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void createEvent_ShouldReturnCreated() throws Exception {
        CreateEventDto dto = new CreateEventDto("New Event", "Desc", "Loc", now, 100);
        EventResponseDto response = new EventResponseDto(eventId, "New Event", "Desc", "Loc", now, 100, 100);
        when(eventService.createEvent(any(CreateEventDto.class))).thenReturn(response);

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/events/" + eventId))
                .andExpect(jsonPath("$.id").value(eventId.toString()));
    }

    @Test
    void createEvent_ShouldReturnBadRequest_WhenInvalidData() throws Exception {
        CreateEventDto invalid = new CreateEventDto("", "", "", null, 0);

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateEvent_ShouldReturnUpdatedEvent() throws Exception {
        UpdateEventDto dto = new UpdateEventDto("Updated", "Desc2", "Loc2", now);
        EventResponseDto response = new EventResponseDto(eventId, "Updated", "Desc2", "Loc2", now, 100, 80);
        when(eventService.updateEvent(eq(eventId), any(UpdateEventDto.class))).thenReturn(response);

        mockMvc.perform(put("/events/{id}", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated"));
    }

    @Test
    void deleteEvent_ShouldReturnNoContent() throws Exception {
        doNothing().when(eventService).deleteEventById(eventId);

        mockMvc.perform(delete("/events/{id}", eventId))
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
