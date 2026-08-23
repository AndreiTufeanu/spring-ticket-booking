import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../../services/api.service';
import { EventDto, CreateEventDto, UpdateEventDto } from '../../../models/event.model';
import { CategoryDto } from '../../../models/category.model';
import { CategoryPicker } from './category-picker/category-picker';

@Component({
  selector: 'app-events-management',
  standalone: true,
  imports: [CommonModule, FormsModule, CategoryPicker],
  templateUrl: './events-management.html',
  styleUrl: './events-management.css',
})
export class EventsManagement implements OnInit {
  private readonly apiService = inject(ApiService);

  // State
  events = signal<EventDto[]>([]);
  categories = signal<CategoryDto[]>([]);
  loading = signal<boolean>(true);

  // Form state
  editingEvent: EventDto | null = null;
  newEvent: CreateEventDto = { title: '', description: '', location: '', eventDate: '', totalSeats: 0, categoryIds: [] };
  editEvent: UpdateEventDto = { title: '', description: '', location: '', eventDate: '', categoryIds: [] };

  ngOnInit() {
    this.loadEvents();
    this.loadCategories();
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

  loadCategories() {
    this.apiService.getCategories().subscribe({
      next: (categories) => this.categories.set(categories)
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
        this.newEvent = { title: '', description: '', location: '', eventDate: '', totalSeats: 0, categoryIds: [] };
      }
    });
  }

  editEventHandler(event: EventDto) {
    this.editingEvent = event;
    this.editEvent = {
      title: event.title,
      description: event.description,
      location: event.location,
      eventDate: new Date(event.eventDate).toISOString().slice(0, 16),
      categoryIds: event.categories.map(c => c.id)
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
        this.editEvent = { title: '', description: '', location: '', eventDate: '', categoryIds: [] };
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
    this.editEvent = { title: '', description: '', location: '', eventDate: '', categoryIds: [] };
  }

  toUtcIso(dateString: string): string {
    if (!dateString) return '';

    const date = new Date(dateString);
    return date.toISOString();
  }
}