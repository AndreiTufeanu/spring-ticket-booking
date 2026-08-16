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
}
