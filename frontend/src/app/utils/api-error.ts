import { HttpErrorResponse } from '@angular/common/http';

export interface ApiFieldError {
  field: string;
  message: string;
}

export interface ApiErrorResponse {
  message?: string;
  errors?: ApiFieldError[];
}

export interface ApiErrorInfo {
  message: string;
  fieldErrors: Record<string, string[]>;
  raw?: ApiErrorResponse;
}

export function parseApiError(err: unknown): ApiErrorInfo {
  const info: ApiErrorInfo = { message: 'Request failed', fieldErrors: {} };
  if (!(err instanceof HttpErrorResponse)) {
    return info;
  }
  const payload = err.error;
  if (payload && typeof payload === 'object') {
    const apiError = payload as ApiErrorResponse;
    info.raw = apiError;
    if (apiError.message) {
      info.message = apiError.message;
    } else if (err.status === 0) {
      info.message = 'Network error';
    } else if (err.status >= 500) {
      info.message = 'Server error';
    }
    if (Array.isArray(apiError.errors)) {
      apiError.errors.forEach((fieldError) => {
        const key = fieldError.field || 'form';
        if (!info.fieldErrors[key]) {
          info.fieldErrors[key] = [];
        }
        if (fieldError.message) {
          info.fieldErrors[key].push(fieldError.message);
        }
      });
    }
    return info;
  }
  if (typeof payload === 'string' && payload.trim()) {
    info.message = payload.trim();
    return info;
  }
  if (err.status === 0) {
    info.message = 'Network error';
  } else if (err.status === 404) {
    info.message = 'Not found';
  } else if (err.status >= 500) {
    info.message = 'Server error';
  }
  return info;
}
