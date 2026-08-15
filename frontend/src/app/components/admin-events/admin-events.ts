import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { EventDto, CreateEventDto, UpdateEventDto } from '../../models/event.model';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-admin-events',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-events.html',
  styleUrl: './admin-events.css',
})
export class AdminEvents implements OnInit {
  private readonly apiService = inject(ApiService);
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);

  // State
  currentUsername: string = '';
  events = signal<EventDto[]>([]);
  loading = signal<boolean>(true);

  // Form state
  editingEvent: EventDto | null = null;
  newEvent: CreateEventDto = { title: '', description: '', location: '', eventDate: '', totalSeats: 0 };
  editEvent: UpdateEventDto = { title: '', description: '', location: '', eventDate: '' };

  ngOnInit() {
    this.currentUsername = this.authService.getUsername() || '';
    this.loadEvents();
  }

  loadEvents() {
    this.loading.set(true);
    this.apiService.getEvents().subscribe({
      next: (events) => {
        this.events.set(events);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      }
    });
  }

  createEvent() {
    const utcEvent: CreateEventDto = {
      ...this.newEvent,
      eventDate: this.toUtcIso(this.newEvent.eventDate)
    };

    this.apiService.createEvent(utcEvent).subscribe({
      next: (event) => {
        this.events.update(events => [...events, event]);
        this.newEvent = { title: '', description: '', location: '', eventDate: '', totalSeats: 0 };
      }
    });
  }

  editEventHandler(event: EventDto) {
    this.editingEvent = event;
    this.editEvent = {
      title: event.title,
      description: event.description,
      location: event.location,
      eventDate: new Date(event.eventDate).toISOString().slice(0, 16)
    };
  }

  updateEvent() {
    if (!this.editingEvent) return;

    const utcEvent: UpdateEventDto = {
      ...this.editEvent,
      eventDate: this.toUtcIso(this.editEvent.eventDate)
    };

    this.apiService.updateEvent(this.editingEvent.id, utcEvent).subscribe({
      next: (updated) => {
        this.events.update(events =>
          events.map(e => e.id === updated.id ? updated : e)
        );
        this.editingEvent = null;
        this.editEvent = { title: '', description: '', location: '', eventDate: '' };
      }
    });
  }

  deleteEvent(id: string) {
    if (!confirm('Delete this event?')) return;

    this.apiService.deleteEvent(id).subscribe({
      next: () => {
        this.events.update(events => events.filter(e => e.id !== id));
      }
    });
  }

  cancelEdit() {
    this.editingEvent = null;
    this.editEvent = { title: '', description: '', location: '', eventDate: '' };
  }

  logout() {
    this.authService.revoke().subscribe({
      next: () => {
        this.authService.removeToken();
        this.router.navigate(['/login']);
      },
      error: () => {
        this.authService.removeToken();
        this.router.navigate(['/login']);
      }
    });
  }

  toUtcIso(dateString: string): string {
    if (!dateString) return '';

    const date = new Date(dateString);
    return date.toISOString();
  }
}
