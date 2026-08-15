import { HttpErrorResponse } from '@angular/common/http';

export interface ApiError {
  type: string;
  title: string;
  status: number;
  errors?: Record<string, string[]>;
}

export function extractErrorMessage(err: HttpErrorResponse): string {
  const body = err.error as ApiError;

  if (!body?.type) return 'Something went wrong. Please try again.';

  switch (body.type) {
    case 'ValidationError':
      return Object.entries(body.errors ?? {})
        .map(([field, messages]) => `${field}: ${messages.join(', ')}`)
        .join(' | ');

    case 'Conflict':
    case 'Unauthorized':
    case 'NotFound':
    case 'Forbidden':
    case 'ServiceUnavailable':
      return body.title;

    default:
      return 'Something went wrong. Please try again.';
  }
}