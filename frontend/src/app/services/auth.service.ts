import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly TOKEN_KEY = 'jwt_token';
  private readonly USERNAME_KEY = 'username';
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiGatewayUrl}/users`;;

  setToken(token: string): void {
    localStorage.setItem(this.TOKEN_KEY, token);
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      if (payload.unique_name) {
        localStorage.setItem(this.USERNAME_KEY, payload.unique_name);
      }
    } catch (error) {
      console.error('Failed to extract claims from token:', error);
    }
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  removeToken(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USERNAME_KEY);
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  getUsername(): string | null {
    return localStorage.getItem(this.USERNAME_KEY);
  }

  getRole(): string | null {
    const token = this.getToken();
    if (!token) return null;
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.role ?? null;
  }

  refresh(): Observable<string> {
    return this.http.post<string>(
      `${this.apiUrl}/refresh`,
      {},
      { withCredentials: true }
    ).pipe(tap((accessToken) => this.setToken(accessToken)));
  }

  revoke(): Observable<void> {
    return this.http.post<void>(
      `${this.apiUrl}/revoke`,
      {},
      { withCredentials: true }
    );
  }
}