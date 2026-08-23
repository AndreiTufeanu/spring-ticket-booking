package com.andreitufeanu.backend.event.service;

import com.andreitufeanu.backend.event.dto.CreateEventDto;
import com.andreitufeanu.backend.event.dto.EventResponseDto;
import com.andreitufeanu.backend.event.dto.UpdateEventDto;
import com.andreitufeanu.backend.event.entity.Category;
import com.andreitufeanu.backend.event.entity.Event;
import com.andreitufeanu.backend.event.mapper.EventMapper;
import com.andreitufeanu.backend.event.repository.CategoryRepository;
import com.andreitufeanu.backend.event.repository.EventRepository;
import com.andreitufeanu.backend.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
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
                .orElseThrow(() -> new NotFoundException("Event not found: " + id));
    }

    @Transactional
    public EventResponseDto createEvent(CreateEventDto dto) {
        Event event = eventMapper.toEntityWithAvailableSeats(dto);

        List<Category> categories = categoryRepository.findAllById(dto.categoryIds());

        Event created = eventRepository.save(event);

        event.setCategories(new ArrayList<>(categories));

        log.info("Event {} created successfully", created.getId());

        return eventMapper.toResponse(created);
    }

    @Transactional
    public EventResponseDto updateEvent(UUID id, UpdateEventDto dto) {
        Event event = eventRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Event not found: " + id));

        eventMapper.updateEntity(dto, event);
        Event updated = eventRepository.save(event);
        log.info("Event {} updated successfully", updated.getId());

        return eventMapper.toResponse(updated);
    }

    @Transactional
    public void deleteEventById(UUID id) {
        eventRepository.deleteById(id);
        log.info("Event {} deleted successfully", id);
    }
}
