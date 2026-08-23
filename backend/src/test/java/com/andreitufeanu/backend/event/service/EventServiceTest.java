package com.andreitufeanu.backend.event.service;

import com.andreitufeanu.backend.event.dto.CreateEventDto;
import com.andreitufeanu.backend.event.dto.EventResponseDto;
import com.andreitufeanu.backend.event.dto.UpdateEventDto;
import com.andreitufeanu.backend.event.entity.Event;
import com.andreitufeanu.backend.event.mapper.EventMapper;
import com.andreitufeanu.backend.event.repository.CategoryRepository;
import com.andreitufeanu.backend.event.repository.EventRepository;
import com.andreitufeanu.backend.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private EventMapper eventMapper;

    @InjectMocks
    private EventService eventService;

    private Event event;
    private UUID eventId;
    private Instant now;

    @BeforeEach
    void setUp() {
        eventId = UUID.randomUUID();
        now = Instant.now();
        event = new Event();
        event.setId(eventId);
        event.setTitle("Test Event");
        event.setDescription("Desc");
        event.setLocation("Loc");
        event.setEventDate(now.plusSeconds(3600));
        event.setTotalSeats(100);
        event.setAvailableSeats(80);
    }

    @Test
    void getUpcomingEvents_ShouldReturnList() {
        when(eventRepository.findByEventDateGreaterThanOrderByEventDateAsc(any(Instant.class)))
                .thenReturn(List.of(event));
        when(eventMapper.toResponse(event)).thenReturn(new EventResponseDto(
                eventId, "Test Event", "Desc", "Loc", event.getEventDate(), 100, 80, List.of()));

        List<EventResponseDto> result = eventService.getUpcomingEvents();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(eventId);
        verify(eventRepository).findByEventDateGreaterThanOrderByEventDateAsc(any(Instant.class));
    }

    @Test
    void getUpcomingEvents_ShouldReturnEmptyList_WhenNoFutureEvents() {
        when(eventRepository.findByEventDateGreaterThanOrderByEventDateAsc(any(Instant.class)))
                .thenReturn(List.of());

        List<EventResponseDto> result = eventService.getUpcomingEvents();

        assertThat(result).isEmpty();
        verify(eventRepository).findByEventDateGreaterThanOrderByEventDateAsc(any(Instant.class));
    }

    @Test
    void getEventById_ShouldReturnEvent_WhenExists() {
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventMapper.toResponse(event)).thenReturn(new EventResponseDto(
                eventId, "Test Event", "Desc", "Loc", event.getEventDate(), 100, 80, List.of()));

        EventResponseDto result = eventService.getEventById(eventId);

        assertThat(result.id()).isEqualTo(eventId);
        verify(eventRepository).findById(eventId);
    }

    @Test
    void getEventById_ShouldThrowNotFoundException_WhenNotFound() {
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.getEventById(eventId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Event not found");
    }

    @Test
    void createEvent_ShouldSaveAndReturnEvent() {
        CreateEventDto dto = new CreateEventDto("New Event", "Desc", "Loc", now, 150, List.of());
        Event eventToSave = new Event();
        eventToSave.setTitle("New Event");
        eventToSave.setTotalSeats(150);
        eventToSave.setAvailableSeats(150);
        when(eventMapper.toEntityWithAvailableSeats(dto)).thenReturn(eventToSave);
        when(categoryRepository.findAllById(anyList())).thenReturn(List.of());
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        when(eventMapper.toResponse(event)).thenReturn(new EventResponseDto(
                eventId, "New Event", "Desc", "Loc", now, 150, 150, List.of()));

        EventResponseDto result = eventService.createEvent(dto);

        assertThat(result.title()).isEqualTo("New Event");
        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(captor.capture());
        assertThat(captor.getValue().getAvailableSeats()).isEqualTo(150);
        verify(eventMapper).toEntityWithAvailableSeats(dto);
        verify(categoryRepository).findAllById(anyList());
    }

    @Test
    void updateEvent_ShouldUpdateAndReturnEvent() {
        UpdateEventDto dto = new UpdateEventDto("Updated Title", "Updated Desc", "Updated Loc", now, null);
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        doAnswer(invocation -> {
            Event e = invocation.getArgument(1);
            e.setTitle(dto.title());
            e.setDescription(dto.description());
            e.setLocation(dto.location());
            e.setEventDate(dto.eventDate());
            return null;
        }).when(eventMapper).updateEntity(any(UpdateEventDto.class), any(Event.class));
        when(eventRepository.save(event)).thenReturn(event);
        when(eventMapper.toResponse(event)).thenReturn(new EventResponseDto(
                eventId, "Updated Title", "Updated Desc", "Updated Loc", now, 100, 80, List.of()));
        EventResponseDto result = eventService.updateEvent(eventId, dto);

        assertThat(result.title()).isEqualTo("Updated Title");
        assertThat(result.availableSeats()).isEqualTo(80);
        verify(eventRepository).save(event);
        verify(eventMapper).updateEntity(dto, event);
    }

    @Test
    void updateEvent_ShouldThrowNotFoundException_WhenNotFound() {
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.updateEvent(eventId, new UpdateEventDto("a", "b", "c", now, null)))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Event not found");
    }

    @Test
    void deleteEventById_ShouldDelete() {
        doNothing().when(eventRepository).deleteById(eventId);
        eventService.deleteEventById(eventId);
        verify(eventRepository).deleteById(eventId);
    }
}
