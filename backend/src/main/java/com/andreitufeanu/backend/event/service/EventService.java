package com.andreitufeanu.backend.event.service;

import com.andreitufeanu.backend.event.dto.CreateEventDto;
import com.andreitufeanu.backend.event.dto.EventResponseDto;
import com.andreitufeanu.backend.event.dto.UpdateEventDto;
import com.andreitufeanu.backend.event.entity.Event;
import com.andreitufeanu.backend.event.mapper.EventMapper;
import com.andreitufeanu.backend.event.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    public List<EventResponseDto> getUpcomingEvents() {
        return eventRepository
                .findByEventDateGreaterThanOrderByEventDateAsc(Instant.now())
                .stream()
                .map(eventMapper::toResponse)
                .toList();
    }

    public EventResponseDto getEventById(UUID id) {
        return eventRepository
                .findById(id)
                .map(eventMapper::toResponse)
                .orElseThrow(() -> new RuntimeException("Event not found: " + id));
    }

    @Transactional
    public EventResponseDto createEvent(CreateEventDto dto) {
        return eventMapper.toResponse(
                eventRepository.save(
                        eventMapper.toEntityWithAvailableSeats(dto)
                )
        );
    }

    @Transactional
    public EventResponseDto updateEvent(UUID id, UpdateEventDto dto) {
        Event event = eventRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found: " + id));

        eventMapper.updateEntity(dto, event);

        return eventMapper.toResponse(eventRepository.save(event));
    }

    @Transactional
    public void deleteEventById(UUID id) {
        eventRepository.deleteById(id);
    }
}
