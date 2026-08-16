package com.andreitufeanu.backend.booking.entity;

import com.andreitufeanu.backend.event.entity.Event;
import com.andreitufeanu.backend.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "bookings", uniqueConstraints =
    @UniqueConstraint(name = "uq_bookings_event_seat", columnNames = {"event_id", "seat_number"}))
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Min(value = 1, message = "Total seats must be at least {value}")
    @Max(value = 1_000_000, message = "Total seats can't exceed {value}")
    @Column(name = "seat_number", nullable = false)
    private int seatNumber;
}
