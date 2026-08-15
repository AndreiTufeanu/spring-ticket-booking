package com.andreitufeanu.backend.booking.repository;

import com.andreitufeanu.backend.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {
}
