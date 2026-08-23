package com.andreitufeanu.backend.booking.repository;

import com.andreitufeanu.backend.booking.entity.Booking;
import com.andreitufeanu.backend.event.entity.Event;
import com.andreitufeanu.backend.user.entity.User;
import com.andreitufeanu.backend.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
class BookingRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookingRepository bookingRepository;

    private User user;
    private Event event1;
    private Event event2;
    private Booking booking1;
    private Booking booking2;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("testuser");
        user.setPasswordHash("encoded123");
        user.setRole(UserRole.ROLE_USER);
        entityManager.persist(user);

        event1 = new Event();
        event1.setTitle("Event 1");
        event1.setLocation("Loc1");
        event1.setEventDate(Instant.now().plusSeconds(3600));
        event1.setTotalSeats(10);
        event1.setAvailableSeats(10);
        entityManager.persist(event1);

        event2 = new Event();
        event2.setTitle("Event 2");
        event2.setLocation("Loc2");
        event2.setEventDate(Instant.now().plusSeconds(7200));
        event2.setTotalSeats(5);
        event2.setAvailableSeats(5);
        entityManager.persist(event2);

        booking1 = new Booking();
        booking1.setUser(user);
        booking1.setEvent(event1);
        booking1.setSeatNumber(1);
        entityManager.persist(booking1);

        booking2 = new Booking();
        booking2.setUser(user);
        booking2.setEvent(event2);
        booking2.setSeatNumber(2);
        entityManager.persist(booking2);

        entityManager.flush();
    }

    @Test
    void findBookingsForUser_ShouldReturnAllBookingsForUser() {
        List<Booking> bookings = bookingRepository.findBookingsForUser(user.getId());

        assertThat(bookings).hasSize(2);
        assertThat(bookings).extracting(Booking::getEvent).extracting(Event::getTitle)
                .containsExactlyInAnyOrder("Event 1", "Event 2");
    }

    @Test
    void findBookingsForUser_ShouldReturnEmpty_WhenUserHasNoBookings() {
        UUID newUserId = UUID.randomUUID();
        List<Booking> bookings = bookingRepository.findBookingsForUser(newUserId);
        assertThat(bookings).isEmpty();
    }
}
