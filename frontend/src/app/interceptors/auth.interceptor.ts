import { HttpInterceptorFn, HttpErrorResponse, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError, BehaviorSubject, filter, take } from 'rxjs';
import { AuthService } from '../services/auth.service';

let isRefreshing = false;
const refreshDone$ = new BehaviorSubject<boolean>(false);

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const authReq = addToken(req, authService);

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      const isAuthEndpoint = authReq.url.includes('/Users/refresh') || authReq.url.includes('/Users/login');
      if (error.status !== 401 || isAuthEndpoint) return throwError(() => error);

      if (isRefreshing) {
        return refreshDone$.pipe(
          filter(done => done),
          take(1),
          switchMap(() => next(addToken(req, authService)))
        );
      }

      isRefreshing = true;
      refreshDone$.next(false);

      return authService.refresh().pipe(
        switchMap(() => {
          isRefreshing = false;
          refreshDone$.next(true);
          return next(addToken(req, authService));
        }),
        catchError((refreshError) => {
          isRefreshing = false;
          refreshDone$.next(false);
          authService.removeToken();
          router.navigate(['/login']);
          return throwError(() => refreshError);
        })
      );
    })
  );
};

function addToken(req: HttpRequest<unknown>, authService: AuthService): HttpRequest<unknown> {
  const token = authService.getToken();
  return req.clone({
    setHeaders: token ? { Authorization: `Bearer ${token}` } : {},
    withCredentials: true
  });
}