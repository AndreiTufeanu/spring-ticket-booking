package com.andreitufeanu.backend.event.repository;

import com.andreitufeanu.backend.event.entity.Event;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID>, JpaSpecificationExecutor<Event> {

    boolean existsByCategoriesId(UUID categoryId);

    @EntityGraph(attributePaths = "categories")
    @Query("select e from Event e")
    List<Event> findAllForReindexing();
}
