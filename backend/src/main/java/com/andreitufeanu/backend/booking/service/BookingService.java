package com.andreitufeanu.backend.booking.service;

import com.andreitufeanu.backend.event.service.EventRagService;
import com.andreitufeanu.backend.booking.dto.BookingDto;
import com.andreitufeanu.backend.booking.dto.CreateBookingDto;
import com.andreitufeanu.backend.booking.entity.Booking;
import com.andreitufeanu.backend.booking.mapper.BookingMapper;
import com.andreitufeanu.backend.booking.repository.BookingRepository;
import com.andreitufeanu.backend.event.entity.Event;
import com.andreitufeanu.backend.event.repository.EventRepository;
import com.andreitufeanu.backend.exceptions.BadRequestException;
import com.andreitufeanu.backend.exceptions.ConflictException;
import com.andreitufeanu.backend.exceptions.ForbiddenException;
import com.andreitufeanu.backend.exceptions.NotFoundException;
import com.andreitufeanu.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BookingService {

    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;
    private final EventRagService eventRagService;

    public List<BookingDto> getUserBookings(UUID userId) {
        return bookingRepository.findBookingsForUser(userId)
                .stream()
                .map(bookingMapper::toDto)
                .toList();
    }

    public BookingDto getBookingById(UUID id) {
        return bookingRepository.findById(id)
                .map(bookingMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Booking " + id));
    }

    @Transactional
    public BookingDto createBooking(UUID userId, CreateBookingDto dto) {
        Event event = eventRepository.findByIdForUpdate(dto.eventId())
                .orElseThrow(() -> new NotFoundException("Event " + dto.eventId()));

        if (event.getAvailableSeats() <= 0)
            throw new ConflictException("This event is fully booked.");

        int seatNumber = firstAvailableSeat(event.getId(), event.getTotalSeats());

        Booking booking = new Booking();
        booking.setUser(userRepository.getReferenceById(userId));
        booking.setEvent(event);
        booking.setSeatNumber(seatNumber);
        bookingRepository.saveAndFlush(booking);

        event.setAvailableSeats(event.getAvailableSeats() - 1);
        eventRepository.save(event);
        eventRagService.updateEvent(event);

        log.info("Created booking {} (seat {}) for user {}", booking.getId(), seatNumber, userId);
        return bookingMapper.toDto(booking);
    }

    @Transactional
    public void cancelBooking(UUID id, UUID userId) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking " + id));

        if (!booking.getUser().getId().equals(userId))
            throw new ForbiddenException("You do not have permission to cancel this booking.");

        Event event = eventRepository.findByIdForUpdate(booking.getEvent().getId())
                .orElseThrow(() -> new NotFoundException("Event " + booking.getEvent().getId()));

        bookingRepository.delete(booking);
        event.setAvailableSeats(event.getAvailableSeats() + 1);
        eventRepository.save(event);
        eventRagService.updateEvent(event);

        log.info("Cancelled booking {} for user {}", id, userId);
    }

    private int firstAvailableSeat(UUID eventId, int totalSeats) {
        Integer seat = bookingRepository.findFirstAvailableSeat(eventId, totalSeats);

        if (seat == null) {
            throw new ConflictException("This event is fully booked.");
        }

        return seat;
    }
}
