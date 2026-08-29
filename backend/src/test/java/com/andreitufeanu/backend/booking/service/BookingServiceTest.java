package com.andreitufeanu.backend.booking.service;

import com.andreitufeanu.backend.booking.dto.BookingDto;
import com.andreitufeanu.backend.booking.dto.CreateBookingDto;
import com.andreitufeanu.backend.booking.entity.Booking;
import com.andreitufeanu.backend.booking.mapper.BookingMapper;
import com.andreitufeanu.backend.booking.repository.BookingRepository;
import com.andreitufeanu.backend.event.entity.Event;
import com.andreitufeanu.backend.event.repository.EventRepository;
import com.andreitufeanu.backend.event.service.EventRagService;
import com.andreitufeanu.backend.exceptions.BadRequestException;
import com.andreitufeanu.backend.exceptions.ConflictException;
import com.andreitufeanu.backend.exceptions.ForbiddenException;
import com.andreitufeanu.backend.exceptions.NotFoundException;
import com.andreitufeanu.backend.user.entity.User;
import com.andreitufeanu.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingMapper bookingMapper;

    @Mock
    private EventRagService eventRagService;

    @InjectMocks
    private BookingService bookingService;

    private UUID userId;
    private UUID eventId;
    private UUID bookingId;
    private User user;
    private Event event;
    private Booking booking;
    private Instant now;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        eventId = UUID.randomUUID();
        bookingId = UUID.randomUUID();
        now = Instant.now();

        user = new User();
        user.setId(userId);

        event = new Event();
        event.setId(eventId);
        event.setTitle("Concert");
        event.setEventDate(now);
        event.setTotalSeats(10);
        event.setAvailableSeats(8);
        event.setLocation("Madison Square Garden");
        event.setDescription("An amazing concert");

        booking = new Booking();
        booking.setId(bookingId);
        booking.setUser(user);
        booking.setEvent(event);
        booking.setSeatNumber(3);
    }

    @Test
    void getUserBookings_ShouldReturnList() {
        when(bookingRepository.findBookingsForUser(userId)).thenReturn(List.of(booking));
        when(bookingMapper.toDto(booking)).thenReturn(
                new BookingDto(bookingId, eventId, 3, "Concert", now,
                        "Madison Square Garden", "An amazing concert")
        );

        List<BookingDto> result = bookingService.getUserBookings(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(bookingId);
        verify(bookingRepository).findBookingsForUser(userId);
    }

    @Test
    void getBookingById_ShouldReturnBooking_WhenExists() {
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingMapper.toDto(booking)).thenReturn(
                new BookingDto(bookingId, eventId, 3, "Concert", now,
                        "Madison Square Garden", "An amazing concert")
        );

        BookingDto result = bookingService.getBookingById(bookingId);

        assertThat(result.id()).isEqualTo(bookingId);
        verify(bookingRepository).findById(bookingId);
    }

    @Test
    void getBookingById_ShouldThrowNotFoundException_WhenNotFound() {
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getBookingById(bookingId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Booking " + bookingId);
    }

    @Test
    void createBooking_Success() {
        CreateBookingDto dto = new CreateBookingDto(eventId, 3);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(userRepository.getReferenceById(userId)).thenReturn(user);
        when(bookingRepository.saveAndFlush(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.toDto(any(Booking.class))).thenReturn(
                new BookingDto(bookingId, eventId, 3, "Concert", now,
                        "Madison Square Garden", "An amazing concert")
        );

        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);

        BookingDto result = bookingService.createBooking(userId, dto);

        assertThat(result.id()).isEqualTo(bookingId);

        verify(bookingRepository).saveAndFlush(bookingCaptor.capture());
        Booking savedBooking = bookingCaptor.getValue();
        assertThat(savedBooking.getSeatNumber()).isEqualTo(3);
        assertThat(savedBooking.getUser()).isEqualTo(user);
        assertThat(savedBooking.getEvent()).isEqualTo(event);

        verify(eventRepository).save(eventCaptor.capture());
        Event savedEvent = eventCaptor.getValue();
        assertThat(savedEvent.getAvailableSeats()).isEqualTo(7);
    }

    @Test
    void createBooking_ThrowsNotFoundException_WhenEventNotFound() {
        CreateBookingDto dto = new CreateBookingDto(eventId, 1);
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(userId, dto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Event " + eventId);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_ThrowsBadRequestException_WhenSeatNumberExceedsTotalSeats() {
        CreateBookingDto dto = new CreateBookingDto(eventId, 20);
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> bookingService.createBooking(userId, dto))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Seat number must be between 1 and 10");
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_ThrowsConflictException_WhenSeatAlreadyTaken() {
        CreateBookingDto dto = new CreateBookingDto(eventId, 3);
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(userRepository.getReferenceById(userId)).thenReturn(user);
        when(bookingRepository.saveAndFlush(any(Booking.class))).thenThrow(DataIntegrityViolationException.class);

        assertThatThrownBy(() -> bookingService.createBooking(userId, dto))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Seat 3 is already taken for this event.");
        verify(eventRepository, never()).save(any());
    }

    @Test
    void cancelBooking_Success_WhenUserOwnsBooking() {
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        bookingService.cancelBooking(bookingId, userId);

        verify(bookingRepository).delete(booking);

        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(eventCaptor.capture());
        Event updatedEvent = eventCaptor.getValue();
        assertThat(updatedEvent.getAvailableSeats()).isEqualTo(9);
    }

    @Test
    void cancelBooking_ThrowsNotFoundException_WhenBookingNotFound() {
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.cancelBooking(bookingId, userId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Booking " + bookingId);
        verify(bookingRepository, never()).delete(any());
    }

    @Test
    void cancelBooking_ThrowsForbiddenException_WhenUserDoesNotOwnBooking() {
        UUID otherUserId = UUID.randomUUID();
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancelBooking(bookingId, otherUserId))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("You do not have permission to cancel this booking.");
        verify(bookingRepository, never()).delete(any());
    }
}
