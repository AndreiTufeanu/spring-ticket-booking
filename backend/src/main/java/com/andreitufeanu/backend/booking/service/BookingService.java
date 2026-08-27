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
        Event event = eventRepository.findById(dto.eventId())
                .orElseThrow(() -> new NotFoundException("Event " + dto.eventId()));

        if (dto.seatNumber() > event.getTotalSeats())
            throw new BadRequestException(
                    "Seat number must be between 1 and " + event.getTotalSeats() + ".");

        Booking booking = new Booking();
        booking.setUser(userRepository.getReferenceById(userId));
        booking.setEvent(event);
        booking.setSeatNumber(dto.seatNumber());

        try {
            bookingRepository.saveAndFlush(booking);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Seat " + dto.seatNumber() + " is already taken for this event.");
        }

        event.setAvailableSeats(event.getAvailableSeats() - 1);
        eventRepository.save(event);
        eventRagService.updateEvent(event);

        log.info("Created booking {} for user {}", booking.getId(), userId);
        return bookingMapper.toDto(booking);
    }

    @Transactional
    public void cancelBooking(UUID id, UUID userId) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking " + id));

        if (!booking.getUser().getId().equals(userId))
            throw new ForbiddenException("You do not have permission to cancel this booking.");

        bookingRepository.delete(booking);

        Event event = booking.getEvent();
        event.setAvailableSeats(event.getAvailableSeats() + 1);
        eventRepository.save(event);
        eventRagService.updateEvent(event);

        log.info("Cancelled booking {} for user {}", id, userId);
    }

}
