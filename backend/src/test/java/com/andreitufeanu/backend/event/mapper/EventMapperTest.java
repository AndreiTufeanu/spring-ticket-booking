package com.andreitufeanu.backend.event.mapper;

import com.andreitufeanu.backend.event.dto.CreateEventDto;
import com.andreitufeanu.backend.event.dto.EventResponseDto;
import com.andreitufeanu.backend.event.dto.UpdateEventDto;
import com.andreitufeanu.backend.event.entity.Event;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EventMapperTest {

    private final EventMapper mapper = Mappers.getMapper(EventMapper.class);

    @Test
    void toResponse_ShouldMapEventToResponseDto() {
        Event event = new Event();
        UUID id = UUID.randomUUID();
        event.setId(id);
        event.setTitle("Test Event");
        event.setDescription("Description");
        event.setLocation("Location");
        Instant now = Instant.now();
        event.setEventDate(now);
        event.setTotalSeats(100);
        event.setAvailableSeats(50);

        EventResponseDto dto = mapper.toResponse(event);

        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.title()).isEqualTo("Test Event");
        assertThat(dto.description()).isEqualTo("Description");
        assertThat(dto.location()).isEqualTo("Location");
        assertThat(dto.eventDate()).isEqualTo(now);
        assertThat(dto.totalSeats()).isEqualTo(100);
        assertThat(dto.availableSeats()).isEqualTo(50);
        assertThat(dto.categories()).isEmpty();
    }

    @Test
    void updateEntity_ShouldMapOnlyGivenFields() {
        Event event = new Event();
        event.setTitle("Old Title");
        event.setDescription("Old Desc");
        event.setLocation("Old Loc");
        Instant oldDate = Instant.now().minusSeconds(1000);
        event.setEventDate(oldDate);
        event.setTotalSeats(50);
        event.setAvailableSeats(40);

        Instant newDate = Instant.now();
        UpdateEventDto dto = new UpdateEventDto("New Title", "New Desc", "New Loc", newDate, null);

        mapper.updateEntity(dto, event);

        assertThat(event.getTitle()).isEqualTo("New Title");
        assertThat(event.getDescription()).isEqualTo("New Desc");
        assertThat(event.getLocation()).isEqualTo("New Loc");
        assertThat(event.getEventDate()).isEqualTo(newDate);
        assertThat(event.getTotalSeats()).isEqualTo(50);
        assertThat(event.getAvailableSeats()).isEqualTo(40);
    }

    @Test
    void toEntity_ShouldMapCreateDtoToEntity_IgnoringIdAndAvailableSeats() {
        Instant now = Instant.now();
        CreateEventDto dto = new CreateEventDto("New Event", "Desc", "Loc", now, 150, List.of());
        Event event = mapper.toEntity(dto);

        assertThat(event.getId()).isNull();
        assertThat(event.getTitle()).isEqualTo("New Event");
        assertThat(event.getDescription()).isEqualTo("Desc");
        assertThat(event.getLocation()).isEqualTo("Loc");
        assertThat(event.getEventDate()).isEqualTo(now);
        assertThat(event.getTotalSeats()).isEqualTo(150);
        assertThat(event.getAvailableSeats()).isZero();
    }

    @Test
    void toEntityWithAvailableSeats_ShouldSetAvailableSeatsEqualToTotalSeats() {
        Instant now = Instant.now();
        CreateEventDto dto = new CreateEventDto("Event", "Desc", "Loc", now, 200, List.of());
        Event event = mapper.toEntityWithAvailableSeats(dto);

        assertThat(event.getTotalSeats()).isEqualTo(200);
        assertThat(event.getAvailableSeats()).isEqualTo(200);
    }
}