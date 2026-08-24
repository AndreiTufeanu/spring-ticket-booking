package com.andreitufeanu.backend.booking.mapper;

import com.andreitufeanu.backend.booking.dto.BookingDto;
import com.andreitufeanu.backend.booking.entity.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "eventTitle", source = "event.title")
    @Mapping(target = "eventDate", source = "event.eventDate")
    @Mapping(target = "location", source = "event.location")
    @Mapping(target = "description", source = "event.description")
    BookingDto toDto(Booking booking);
}
