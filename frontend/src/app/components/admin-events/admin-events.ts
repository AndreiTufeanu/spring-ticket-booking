import { Component, signal, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { EventsManagement } from './events-management/events-management';
import { CategoriesManagement } from './categories-management/categories-management';

type AdminTab = 'events' | 'categories';

@Component({
  selector: 'app-admin-events',
  standalone: true,
  imports: [EventsManagement, CategoriesManagement],
  templateUrl: './admin-events.html',
  styleUrl: './admin-events.css',
})
export class AdminEvents {
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);

  currentUsername: string = this.authService.getUsername() || '';
  activeTab = signal<AdminTab>('events');

  setTab(tab: AdminTab) {
    this.activeTab.set(tab);
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
}