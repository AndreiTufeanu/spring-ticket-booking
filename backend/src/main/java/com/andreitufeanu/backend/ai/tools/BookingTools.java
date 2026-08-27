package com.andreitufeanu.backend.ai.tools;

import com.andreitufeanu.backend.booking.entity.Booking;
import com.andreitufeanu.backend.booking.repository.BookingRepository;
import com.andreitufeanu.backend.event.entity.Event;
import com.andreitufeanu.backend.event.repository.EventRepository;
import com.andreitufeanu.backend.event.specification.EventSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BookingTools {

    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;

    @Tool(description = """
        Lists every booking the current user currently has, including each
        booked event's ID and title. Takes no parameters and does not require
        any event to already be present in the conversation context.

        Use this as the default way to answer any question about the user's
        bookings: what they've booked, whether they've booked one of several
        events, or whether they've booked an event mentioned earlier in the
        conversation whose ID is not currently visible. Match events by
        comparing titles yourself if no ID is available.

        Prefer this over isEventBooked whenever an event's ID is not directly
        visible in the current context.
        """)
    public List<BookingSummary> getMyBookings(ToolContext toolContext) {
        UUID userId = currentUserId(toolContext);
        return bookingRepository.findBookingsForUser(userId).stream()
                .map(BookingTools::toSummary)
                .toList();
    }

    @Tool(description = """
        Checks whether the current user already has a booking for ONE specific
        event, identified by its ID.

        Only call this when the event's ID is directly visible in the current
        context, formatted as [ID: ...]. If no ID is currently visible, do not
        call this tool and do not ask the user to supply one — call
        getMyBookings instead and match by event title.
        """)
    public boolean isEventBooked(
            @ToolParam(description = "UUID of the event, taken from [ID: ...] in the event context")
            String eventId,
            ToolContext toolContext) {
        UUID userId = currentUserId(toolContext);
        UUID eventUuid = UUID.fromString(eventId);
        return bookingRepository.findBookingsForUser(userId).stream()
                .anyMatch(b -> b.getEvent().getId().equals(eventUuid));
    }

    @Tool(description = """
        Finds upcoming events within a given number of days that the current
        user has NOT booked a ticket for yet. Use this for questions like
        "what's on next week that I haven't booked" or "any events this month
        I still need a ticket for". Requires no prior event context.
        """)
    public List<EventSummary> getUpcomingEventsWithoutBooking(
            @ToolParam(description = "How many days ahead to look, e.g. 7 for next week")
            int withinDays,
            ToolContext toolContext) {
        UUID userId = currentUserId(toolContext);

        Instant now = Instant.now();
        Instant until = now.plus(withinDays, ChronoUnit.DAYS);

        Set<UUID> bookedEventIds = bookingRepository.findBookingsForUser(userId).stream()
                .map(b -> b.getEvent().getId())
                .collect(Collectors.toSet());

        return eventRepository
                .findAll(EventSpecifications.isUpcoming(now), Sort.by(Sort.Direction.ASC, "eventDate"))
                .stream()
                .filter(e -> !e.getEventDate().isAfter(until))
                .filter(e -> !bookedEventIds.contains(e.getId()))
                .map(BookingTools::toEventSummary)
                .toList();
    }

    private UUID currentUserId(ToolContext toolContext) {
        Object userId = toolContext.getContext().get("userId");
        if (userId == null) {
            throw new IllegalStateException("userId missing from tool context");
        }
        return UUID.fromString(userId.toString());
    }

    private static BookingSummary toSummary(Booking booking) {
        Event event = booking.getEvent();
        return new BookingSummary(
                booking.getId().toString(),
                event.getId().toString(),
                event.getTitle(),
                String.valueOf(event.getEventDate()),
                event.getLocation(),
                booking.getSeatNumber()
        );
    }

    private static EventSummary toEventSummary(Event event) {
        return new EventSummary(
                event.getId().toString(),
                event.getTitle(),
                String.valueOf(event.getEventDate()),
                event.getLocation()
        );
    }

    public record BookingSummary(
            String bookingId, String eventId, String eventTitle,
            String eventDate, String eventLocation, int seatNumber) {
    }

    public record EventSummary(
            String eventId, String eventTitle, String eventDate, String eventLocation) {
    }
}