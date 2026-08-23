package com.andreitufeanu.backend.booking.mapper;

import com.andreitufeanu.backend.booking.dto.BookingDto;
import com.andreitufeanu.backend.booking.entity.Booking;
import com.andreitufeanu.backend.event.entity.Event;
import com.andreitufeanu.backend.user.entity.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class BookingMapperTest {

    private final BookingMapper mapper = Mappers.getMapper(BookingMapper.class);

    @Test
    void toDto_ShouldMapBookingToBookingDto() {
        UUID bookingId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Instant eventDate = Instant.now();

        User user = new User();
        user.setId(userId);

        Event event = new Event();
        event.setId(eventId);
        event.setTitle("Concert");
        event.setEventDate(eventDate);

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setUser(user);
        booking.setEvent(event);
        booking.setSeatNumber(5);

        BookingDto dto = mapper.toDto(booking);

        assertThat(dto.id()).isEqualTo(bookingId);
        assertThat(dto.eventId()).isEqualTo(eventId);
        assertThat(dto.seatNumber()).isEqualTo(5);
        assertThat(dto.eventTitle()).isEqualTo("Concert");
        assertThat(dto.eventDate()).isEqualTo(eventDate);
    }
}
