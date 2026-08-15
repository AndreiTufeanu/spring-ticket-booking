package com.andreitufeanu.backend.event.mapper;

import com.andreitufeanu.backend.event.dto.CreateEventDto;
import com.andreitufeanu.backend.event.dto.EventResponseDto;
import com.andreitufeanu.backend.event.dto.UpdateEventDto;
import com.andreitufeanu.backend.event.entity.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EventMapper {

    EventResponseDto toResponse(Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "availableSeats", ignore = true)
    void updateEntity(UpdateEventDto updateEventDto, @MappingTarget Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "availableSeats", ignore = true)
    Event toEntity(CreateEventDto dto);

    default Event toEntityWithAvailableSeats(CreateEventDto dto) {
        Event event = toEntity(dto);
        event.setAvailableSeats(event.getTotalSeats());
        return event;
    }
}
