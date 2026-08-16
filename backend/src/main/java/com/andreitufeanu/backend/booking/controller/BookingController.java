package com.andreitufeanu.backend.booking.controller;

import com.andreitufeanu.backend.booking.dto.BookingDto;
import com.andreitufeanu.backend.booking.dto.CreateBookingDto;
import com.andreitufeanu.backend.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bookings")
public class BookingController {
    private final BookingService bookingService;

    @GetMapping
    public ResponseEntity<List<BookingDto>> getUserBookings(Authentication authentication) {
        return ResponseEntity.ok(bookingService.getUserBookings(userId(authentication)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingDto> getBookingById(@PathVariable UUID id) {
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }

    @PostMapping
    public ResponseEntity<BookingDto> createBooking(@RequestBody @Valid CreateBookingDto dto,
                                                    Authentication authentication) {
        BookingDto created = bookingService.createBooking(userId(authentication), dto);
        return ResponseEntity.created(URI.create("/bookings/" + created.id())).body(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelBooking(@PathVariable UUID id,
                                              Authentication authentication) {
        bookingService.cancelBooking(id, userId(authentication));
        return ResponseEntity.noContent().build();
    }

    private UUID userId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}
