import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CreateEventDto, UpdateEventDto, EventDto } from '../models/event.model';
import { BookingDto, CreateBookingDto } from '../models/booking.model';
import { environment } from '../../environments/environment';
import { API_PATHS } from '../constants/api-paths';

@Injectable({
    providedIn: 'root'
})
export class ApiService {
    private readonly apiUrl = environment.apiGatewayUrl;
    private readonly http = inject(HttpClient);

    // Auth
    login(username: string, password: string): Observable<string> {
    return this.http.post<string>(
        `${this.apiUrl}/${API_PATHS.LOGIN}`,
        { username, password },
        { withCredentials: true }
    );
    }

    register(username: string, password: string): Observable<void> {
        return this.http.post<void>(
            `${this.apiUrl}/${API_PATHS.REGISTER}`,
            { username, password }
        );
    }

    // Events
    getEvents(): Observable<EventDto[]> {
        return this.http.get<EventDto[]>(`${this.apiUrl}/${API_PATHS.EVENTS}`);
    }

    getEventById(id: string): Observable<EventDto> {
        return this.http.get<EventDto>(`${this.apiUrl}/${API_PATHS.EVENTS}/${id}`);
    }

    createEvent(event: CreateEventDto): Observable<EventDto> {
        return this.http.post<EventDto>(`${this.apiUrl}/${API_PATHS.EVENTS}`, event);
    }

    updateEvent(id: string, event: UpdateEventDto): Observable<EventDto> {
        return this.http.put<EventDto>(`${this.apiUrl}/${API_PATHS.EVENTS}/${id}`, event);
    }

    deleteEvent(id: string): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${API_PATHS.EVENTS}/${id}`);
    }

    // Bookings
    getBookings(): Observable<BookingDto[]> {
        return this.http.get<BookingDto[]>(`${this.apiUrl}/${API_PATHS.BOOKINGS}`);
    }

    createBooking(booking: CreateBookingDto): Observable<BookingDto> {
        return this.http.post<BookingDto>(`${this.apiUrl}/${API_PATHS.BOOKINGS}`, booking);
    }

    cancelBooking(bookingId: string): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${API_PATHS.BOOKINGS}/${bookingId}`);
    }
}
