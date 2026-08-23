package com.andreitufeanu.backend.event.repository;

import com.andreitufeanu.backend.event.entity.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
class EventRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EventRepository eventRepository;

    private Event futureEvent1;
    private Event futureEvent2;
    private Event pastEvent;

    @BeforeEach
    void setUp() {
        Instant now = Instant.now();

        futureEvent1 = new Event();
        futureEvent1.setTitle("Future 1");
        futureEvent1.setLocation("Location A");
        futureEvent1.setEventDate(now.plus(1, ChronoUnit.DAYS));
        futureEvent1.setTotalSeats(100);
        futureEvent1.setAvailableSeats(100);
        entityManager.persist(futureEvent1);

        futureEvent2 = new Event();
        futureEvent2.setTitle("Future 2");
        futureEvent2.setLocation("Location B");
        futureEvent2.setEventDate(now.plus(2, ChronoUnit.DAYS));
        futureEvent2.setTotalSeats(50);
        futureEvent2.setAvailableSeats(50);
        entityManager.persist(futureEvent2);

        pastEvent = new Event();
        pastEvent.setTitle("Past Event");
        pastEvent.setLocation("Location C");
        pastEvent.setEventDate(now.minus(1, ChronoUnit.DAYS));
        pastEvent.setTotalSeats(30);
        pastEvent.setAvailableSeats(30);
        entityManager.persist(pastEvent);

        entityManager.flush();
    }

    @Test
    void findByEventDateGreaterThanOrderByEventDateAsc_ShouldReturnFutureEventsSorted() {
        List<Event> events = eventRepository.findByEventDateGreaterThanOrderByEventDateAsc(Instant.now());

        assertThat(events).hasSize(2);
        assertThat(events.get(0).getTitle()).isEqualTo("Future 1");
        assertThat(events.get(1).getTitle()).isEqualTo("Future 2");
        assertThat(events).doesNotContain(pastEvent);
    }

    @Test
    void findByEventDateGreaterThanOrderByEventDateAsc_ShouldReturnEmpty_WhenNoFutureEvents() {
        eventRepository.deleteAll();
        entityManager.flush();

        List<Event> events = eventRepository.findByEventDateGreaterThanOrderByEventDateAsc(Instant.now());
        assertThat(events).isEmpty();
    }
}
