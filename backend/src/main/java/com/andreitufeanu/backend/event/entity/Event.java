package com.andreitufeanu.backend.event.entity;

import com.andreitufeanu.backend.booking.entity.Booking;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotBlank(message = "Title is required")
    @Size(max = 50, message = "Title can't have more than {max} characters")
    @Column(name = "title", nullable = false)
    private String title;

    @Size(max = 1000, message = "Description can't have more than {max} characters")
    @Column(name = "description")
    private String description;

    @NotBlank(message = "Location of the event is required")
    @Size(max = 300, message = "Location can't have more than {max} characters")
    @Column(name = "location", nullable = false)
    private String location;

    @NotNull(message = "Date of the event is required")
    @Column(name = "event_date", nullable = false)
    private Instant eventDate;

    @Min(value = 1, message = "Total seats must be at least {value}")
    @Max(value = 1_000_000, message = "Total seats can't exceed {value}")
    @Column(name = "total_seats", nullable = false)
    private int totalSeats;

    @Min(value = 0, message = "Available seats must be at least {value}")
    @Max(value = 1_000_000, message = "Available seats can't exceed {value}")
    @Column(name = "available_seats", nullable = false)
    private int availableSeats;

    @OneToMany(mappedBy = "event", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Booking> bookings = new ArrayList<>();
}
