import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { NotificationService } from '../services/notification.service';
import { extractErrorMessage } from '../utils/api-error.util';

const SILENT_URLS = ['/users/login', '/users/register', '/users/refresh'];

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const notificationService = inject(NotificationService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      const isSilent = SILENT_URLS.some(url => req.url.includes(url));

      if (!isSilent) {
        const message = error.status === 500
          ? 'Server error. Please try again later.'
          : extractErrorMessage(error);

        notificationService.showError(message);
      }

      return throwError(() => error);
    })
  );
};