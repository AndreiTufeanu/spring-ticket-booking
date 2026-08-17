package com.andreitufeanu.backend.event.controller;

import com.andreitufeanu.backend.event.dto.CreateEventDto;
import com.andreitufeanu.backend.event.dto.EventResponseDto;
import com.andreitufeanu.backend.event.dto.UpdateEventDto;
import com.andreitufeanu.backend.event.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<List<EventResponseDto>> getAllEvents() {
        return ResponseEntity.ok().body(eventService.getUpcomingEvents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDto> getEventById(@PathVariable UUID id) {
        return ResponseEntity.ok().body(eventService.getEventById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventResponseDto> createEvent(@RequestBody @Valid CreateEventDto createEventDto) {
        var createdEvent = eventService.createEvent(createEventDto);

        return ResponseEntity
                .created(URI.create("/events/" + createdEvent.id()))
                .body(createdEvent);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventResponseDto> updateEvent(@PathVariable UUID id, @RequestBody @Valid UpdateEventDto updateEventDto) {
        return ResponseEntity.ok().body(eventService.updateEvent(id, updateEventDto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEvent(@PathVariable UUID id) {
        eventService.deleteEventById(id);
        return ResponseEntity.noContent().build();
    }

}
