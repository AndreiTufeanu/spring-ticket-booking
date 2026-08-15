import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CreateEventDto, UpdateEventDto, EventDto } from '../models/event.model';
import { BookingDto, CreateBookingDto } from '../models/booking.model';
import { environment } from '../../environments/environment';

@Injectable({
    providedIn: 'root'
})
export class ApiService {
    private readonly apiUrl = environment.apiGatewayUrl;
    private readonly EVENTS = 'Events';
    private readonly BOOKINGS = 'Bookings';
    private readonly USERS = 'Users';
    private readonly http = inject(HttpClient);

    // Auth
    login(username: string, password: string): Observable<string> {
        return this.http.post<string>(
            `${this.apiUrl}/${this.USERS}/login`,
            { username, password },
            { withCredentials: true }
        );
    }

    register(username: string, password: string): Observable<void> {
        return this.http.post<void>(
            `${this.apiUrl}/${this.USERS}/register`,
            { username, password }
        );
    }

    // Events
    getEvents(): Observable<EventDto[]> {
        return this.http.get<EventDto[]>(`${this.apiUrl}/${this.EVENTS}`);
    }

    getEventById(id: string): Observable<EventDto> {
        return this.http.get<EventDto>(`${this.apiUrl}/${this.EVENTS}/${id}`);
    }

    createEvent(event: CreateEventDto): Observable<EventDto> {
        return this.http.post<EventDto>(`${this.apiUrl}/${this.EVENTS}`, event);
    }

    updateEvent(id: string, event: UpdateEventDto): Observable<EventDto> {
        return this.http.put<EventDto>(`${this.apiUrl}/${this.EVENTS}/${id}`, event);
    }

    deleteEvent(id: string): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${this.EVENTS}/${id}`);
    }

    // Bookings
    getBookings(): Observable<BookingDto[]> {
        return this.http.get<BookingDto[]>(`${this.apiUrl}/${this.BOOKINGS}`);
    }

    createBooking(booking: CreateBookingDto): Observable<BookingDto> {
        return this.http.post<BookingDto>(`${this.apiUrl}/${this.BOOKINGS}`, booking);
    }

    cancelBooking(bookingId: string): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${this.BOOKINGS}/${bookingId}`);
    }
}
