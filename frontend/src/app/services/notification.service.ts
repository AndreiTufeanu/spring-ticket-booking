import { Injectable, signal } from '@angular/core';

export interface Notification {
  message: string;
  type: 'error' | 'success' | 'info';
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
  notification = signal<Notification | null>(null);

  showError(message: string) {
    this.notification.set({ message, type: 'error' });
    setTimeout(() => this.notification.set(null), 4000);
  }

  showSuccess(message: string) {
    this.notification.set({ message, type: 'success' });
    setTimeout(() => this.notification.set(null), 3000);
  }
}