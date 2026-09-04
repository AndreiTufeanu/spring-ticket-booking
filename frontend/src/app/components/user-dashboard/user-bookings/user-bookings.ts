import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../../services/api.service';
import { EventDto } from '../../../models/event.model';
import { BookingDto } from '../../../models/booking.model';
import { CategoryDto } from '../../../models/category.model';
import { NotificationService } from '../../../services/notification.service';
import { DescriptionModal } from '../../description-modal/description-modal';

@Component({
  selector: 'app-user-bookings',
  standalone: true,
  imports: [CommonModule, FormsModule, DescriptionModal],
  templateUrl: './user-bookings.html',
  styleUrl: './user-bookings.css',
})
export class UserBookings implements OnInit {
  private readonly apiService = inject(ApiService);
  private readonly notificationService = inject(NotificationService);

  // State
  events = signal<EventDto[]>([]);
  bookings = signal<BookingDto[]>([]);
  loading = signal<boolean>(true);

  // Description modal
  activeDescription = signal<{ title: string; description: string } | null>(null);

  // Filtering
  categories = signal<CategoryDto[]>([]);
  selectedCategoryIds = signal<string[]>([]);
  pendingCategoryIds = signal<string[]>([]);
  filterPanelOpen = signal<boolean>(false);
  categorySearchTerm = signal<string>('');

  filteredCategories = computed(() => {
    const term = this.categorySearchTerm().trim().toLowerCase();
    if (!term) return this.categories();
    return this.categories().filter(c => c.name.toLowerCase().includes(term));
  });

  selectedCategoryObjects = computed(() => {
    const byId = new Map(this.categories().map(c => [c.id, c]));
    return this.selectedCategoryIds()
      .map(id => byId.get(id))
      .filter((c): c is CategoryDto => !!c);
  });

  ngOnInit() {
    this.loadCategories();
    this.loadData();
  }

  loadCategories() {
    this.apiService.getCategories().subscribe({
      next: (categories) => this.categories.set(categories)
    });
  }

  loadData() {
    this.loading.set(true);

    this.apiService.getEvents(this.selectedCategoryIds()).subscribe({
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

  bookSeat(eventId: string) {
    this.apiService.createBooking({ eventId }).subscribe({
      next: (booking) => {
        this.bookings.update(b => [...b, booking]);
        this.events.update(events =>
          events.map(e => e.id === eventId ? { ...e, availableSeats: e.availableSeats - 1 } : e)
        );
        this.notificationService.showSuccess(`Booked seat #${booking.seatNumber}!`);
      }
    });
  }

  cancelBooking(bookingId: string) {
    if (!confirm('Cancel this booking?')) return;

    const booking = this.bookings().find(b => b.id === bookingId);

    this.apiService.cancelBooking(bookingId).subscribe({
      next: () => {
        this.bookings.update(b => b.filter(booking => booking.id !== bookingId));
        if (booking) {
          this.events.update(events =>
            events.map(e => e.id === booking.eventId ? { ...e, availableSeats: e.availableSeats + 1 } : e)
          );
        }
      }
    });
  }

  openDescription(title: string, description: string) {
    this.activeDescription.set({ title, description });
  }

  closeDescription() {
    this.activeDescription.set(null);
  }

  goToEvent(eventId: string) {
    const target = document.getElementById(`event-card-${eventId}`);

    if (!target) {
      this.notificationService.showError(
        "This event isn't currently visible — it may be filtered out or no longer upcoming."
      );
      return;
    }

    target.scrollIntoView({ behavior: 'smooth', block: 'center' });

    target.classList.remove('event-highlight-fade');
    target.classList.add('event-highlight');

    setTimeout(() => {
      target.classList.remove('event-highlight');
      target.classList.add('event-highlight-fade');
    }, 1400);

    setTimeout(() => {
      target.classList.remove('event-highlight-fade');
    }, 1400 + 1300);
  }

  toggleFilterPanel() {
    if (!this.filterPanelOpen()) {
      this.pendingCategoryIds.set([...this.selectedCategoryIds()]);
      this.categorySearchTerm.set('');
    }
    this.filterPanelOpen.update(open => !open);
  }

  isPending(categoryId: string): boolean {
    return this.pendingCategoryIds().includes(categoryId);
  }

  togglePendingCategory(categoryId: string) {
    this.pendingCategoryIds.update(ids =>
      ids.includes(categoryId) ? ids.filter(id => id !== categoryId) : [...ids, categoryId]
    );
  }

  saveFilters() {
    this.selectedCategoryIds.set([...this.pendingCategoryIds()]);
    this.filterPanelOpen.set(false);
    this.reloadEvents();
  }

  clearAllFilters() {
    this.selectedCategoryIds.set([]);
    this.pendingCategoryIds.set([]);
    this.filterPanelOpen.set(false);
    this.reloadEvents();
  }

  removeSelectedCategory(categoryId: string) {
    this.selectedCategoryIds.update(ids => ids.filter(id => id !== categoryId));
    this.pendingCategoryIds.update(ids => ids.filter(id => id !== categoryId));
    this.reloadEvents();
  }

  private reloadEvents() {
    this.apiService.getEvents(this.selectedCategoryIds()).subscribe({
      next: (events) => this.events.set(events)
    });
  }
}