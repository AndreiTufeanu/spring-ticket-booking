package com.andreitufeanu.backend.event.repository;

import com.andreitufeanu.backend.event.entity.Event;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID>, JpaSpecificationExecutor<Event> {

    boolean existsByCategoriesId(UUID categoryId);

    @EntityGraph(attributePaths = "categories")
    @Query("select e from Event e")
    List<Event> findAllForReindexing();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Event e where e.id = :id")
    Optional<Event> findByIdForUpdate(@Param("id") UUID id);
}
