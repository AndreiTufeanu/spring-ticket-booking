package com.andreitufeanu.backend.booking.repository;

import com.andreitufeanu.backend.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId")
    List<Booking> findBookingsForUser(@Param("userId") UUID userId);

    @Query(value = """
        SELECT 1
        WHERE NOT EXISTS (
            SELECT 1
            FROM bookings
            WHERE event_id = :eventId
              AND seat_number = 1
        )
        
        UNION ALL
        
        SELECT b.seat_number + 1
        FROM bookings b
        LEFT JOIN bookings next
            ON next.event_id = b.event_id
           AND next.seat_number = b.seat_number + 1
        WHERE b.event_id = :eventId
          AND next.seat_number IS NULL
          AND b.seat_number < :totalSeats
        
        ORDER BY 1
        LIMIT 1
        """, nativeQuery = true)
    Integer findFirstAvailableSeat(
            @Param("eventId") UUID eventId,
            @Param("totalSeats") int totalSeats
    );
}
