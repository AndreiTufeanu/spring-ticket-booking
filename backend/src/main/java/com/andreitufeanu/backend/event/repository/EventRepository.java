package com.andreitufeanu.backend.event.repository;

import com.andreitufeanu.backend.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {

    public List<Event> findByEventDateGreaterThanOrderByEventDateAsc(Instant currentDate);

}
