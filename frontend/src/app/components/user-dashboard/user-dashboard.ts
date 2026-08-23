import { Component, signal, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { UserBookings } from './user-bookings/user-bookings';
import { AiHelper } from './ai-helper/ai-helper';

type UserTab = 'bookings' | 'ai-helper';

@Component({
  selector: 'app-user-dashboard',
  standalone: true,
  imports: [UserBookings, AiHelper],
  templateUrl: './user-dashboard.html',
  styleUrl: './user-dashboard.css',
})
export class UserDashboard {
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);

  currentUsername: string = this.authService.getUsername() || '';
  activeTab = signal<UserTab>('bookings');

  setTab(tab: UserTab) {
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