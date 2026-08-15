package com.andreitufeanu.backend.event.repository;

import com.andreitufeanu.backend.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {
}
