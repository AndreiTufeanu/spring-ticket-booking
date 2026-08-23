import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { EventDto } from '../../models/event.model';
import { CreateBookingDto, BookingDto } from '../../models/booking.model';

@Component({
  selector: 'app-user-bookings',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './user-bookings.html',
  styleUrl: './user-bookings.css',
})
export class UserBookings implements OnInit {
  private readonly apiService = inject(ApiService);

  // State
  events = signal<EventDto[]>([]);
  bookings = signal<BookingDto[]>([]);
  loading = signal<boolean>(true);

  selectedSeats: Record<string, number> = {};

  ngOnInit() {
    this.loadData();
  }

  loadData() {
    this.loading.set(true);

    this.apiService.getEvents().subscribe({
      next: (events) => {
        this.events.set(events);
        this.loadBookings();
      },
      error: () => {
        this.loading.set(false);
      }
    });
  }

  loadBookings() {
    this.apiService.getBookings().subscribe({
      next: (bookings) => {
        this.bookings.set(bookings);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      }
    });
  }

  getSelectedSeat(eventId: string): number {
    return this.selectedSeats[eventId] || 1;
  }

  setSelectedSeat(eventId: string, seatNumber: number) {
    this.selectedSeats[eventId] = seatNumber;
  }

  bookSeat(eventId: string) {
    const seatNumber = this.getSelectedSeat(eventId);
    if (seatNumber < 1) {
      return;
    }

    this.apiService.createBooking({ eventId, seatNumber }).subscribe({
      next: (booking) => {
        this.bookings.update(b => [...b, booking]);
        this.selectedSeats[eventId] = 1;
      }
    });
  }

  cancelBooking(bookingId: string) {
    if (!confirm('Cancel this booking?')) return;

    this.apiService.cancelBooking(bookingId).subscribe({
      next: () => this.bookings.update(b => b.filter(booking => booking.id !== bookingId))
    });
  }
}