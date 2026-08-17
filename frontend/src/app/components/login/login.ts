import { Component, signal, inject } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { ApiService } from '../../services/api.service';
import { extractErrorMessage } from '../../utils/api-error.util';
import { HttpErrorResponse } from '@angular/common/http';
import { ROLES } from '../../constants/role';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  private readonly router = inject(Router);
  private readonly apiService = inject(ApiService);
  private readonly authService = inject(AuthService);

  username = '';
  password = '';
  isRegister = false;
  success = signal<string>('');
  error = signal<string>('');

  submit() {
    this.error.set('');
    this.success.set('');

    if (this.isRegister) {
      this.apiService.register(this.username, this.password).subscribe({
        next: () => {
          this.success.set('Registration successful! Please log in.');
          this.isRegister = false;
          this.username = '';
          this.password = '';
        },
        error: (err: HttpErrorResponse) => this.error.set(extractErrorMessage(err))
      });
    } else {
      this.apiService.login(this.username, this.password).subscribe({
        next: (accessToken: string) => {
          this.authService.setToken(accessToken);
          const role = this.authService.getRole();
          this.router.navigate([role === ROLES.ADMIN ? '/admin' : '/user']);
        },
        error: (err: HttpErrorResponse) => this.error.set(extractErrorMessage(err))
      });
    }
  }

  toggleMode() {
    this.isRegister = !this.isRegister;
    this.error.set('');
    this.success.set('');
    this.username = '';
    this.password = '';
  }
}
