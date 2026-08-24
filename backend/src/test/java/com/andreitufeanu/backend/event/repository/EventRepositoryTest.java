package com.andreitufeanu.backend.event.repository;

import com.andreitufeanu.backend.event.entity.Category;
import com.andreitufeanu.backend.event.entity.Event;
import com.andreitufeanu.backend.event.specification.EventSpecifications;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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

    private Category musicCategory;
    private Category techCategory;

    @BeforeEach
    void setUp() {
        Instant now = Instant.now();

        musicCategory = new Category();
        musicCategory.setName("Music");
        entityManager.persist(musicCategory);

        techCategory = new Category();
        techCategory.setName("Tech");
        entityManager.persist(techCategory);

        futureEvent1 = new Event();
        futureEvent1.setTitle("Future 1");
        futureEvent1.setLocation("Location A");
        futureEvent1.setEventDate(now.plus(1, ChronoUnit.DAYS));
        futureEvent1.setTotalSeats(100);
        futureEvent1.setAvailableSeats(100);
        futureEvent1.setCategories(new ArrayList<>(List.of(musicCategory)));
        entityManager.persist(futureEvent1);

        futureEvent2 = new Event();
        futureEvent2.setTitle("Future 2");
        futureEvent2.setLocation("Location B");
        futureEvent2.setEventDate(now.plus(2, ChronoUnit.DAYS));
        futureEvent2.setTotalSeats(50);
        futureEvent2.setAvailableSeats(50);
        futureEvent2.setCategories(new ArrayList<>(List.of(musicCategory, techCategory)));
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
    void findAll_WithIsUpcomingSpec_ShouldReturnFutureEventsSorted() {
        Specification<Event> spec = Specification.where(EventSpecifications.isUpcoming(Instant.now()));

        List<Event> events = eventRepository.findAll(spec, Sort.by(Sort.Direction.ASC, "eventDate"));

        assertThat(events).hasSize(2);
        assertThat(events.get(0).getTitle()).isEqualTo("Future 1");
        assertThat(events.get(1).getTitle()).isEqualTo("Future 2");
        assertThat(events).doesNotContain(pastEvent);
    }

    @Test
    void findAll_WithIsUpcomingSpec_ShouldReturnEmpty_WhenNoFutureEvents() {
        eventRepository.deleteAll();
        entityManager.flush();

        Specification<Event> spec = Specification.where(EventSpecifications.isUpcoming(Instant.now()));
        List<Event> events = eventRepository.findAll(spec, Sort.by(Sort.Direction.ASC, "eventDate"));

        assertThat(events).isEmpty();
    }

    @Test
    void findAll_WithCategoryFilter_ShouldReturnOnlyEventsHavingAllGivenCategories() {
        Specification<Event> spec = Specification
                .where(EventSpecifications.isUpcoming(Instant.now()))
                .and(EventSpecifications.hasAllCategories(List.of(musicCategory.getId(), techCategory.getId())));

        List<Event> events = eventRepository.findAll(spec, Sort.by(Sort.Direction.ASC, "eventDate"));

        assertThat(events).hasSize(1);
        assertThat(events.get(0).getTitle()).isEqualTo("Future 2");
    }

    @Test
    void findAll_WithNullCategoryFilter_ShouldReturnAllUpcomingEvents() {
        Specification<Event> spec = Specification
                .where(EventSpecifications.isUpcoming(Instant.now()))
                .and(EventSpecifications.hasAllCategories(null));

        List<Event> events = eventRepository.findAll(spec, Sort.by(Sort.Direction.ASC, "eventDate"));

        assertThat(events).hasSize(2);
    }
}