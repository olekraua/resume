import { HttpHeaders, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';

import { API_BASE_URL } from '../config/api-config';
import { CsrfService } from '../services/csrf.service';

const API_PREFIX = '/api';
const SAFE_METHODS = new Set(['GET', 'HEAD', 'OPTIONS', 'TRACE']);

export const apiBaseInterceptor: HttpInterceptorFn = (req, next) => {
  if (!req.url.startsWith(API_PREFIX)) {
    return next(req);
  }

  const baseUrl = normalizeBaseUrl(inject(API_BASE_URL));
  const csrfService = inject(CsrfService);
  const url = baseUrl ? `${baseUrl}${req.url}` : req.url;
  const headers = applyCsrfHeaders(req.headers, req.method, csrfService);

  return next(req.clone({ url, withCredentials: true, headers }));
};

function normalizeBaseUrl(baseUrl: string | null | undefined): string {
  if (!baseUrl) {
    return '';
  }
  return baseUrl.endsWith('/') ? baseUrl.slice(0, -1) : baseUrl;
}

function applyCsrfHeaders(headers: HttpHeaders, method: string, csrfService: CsrfService): HttpHeaders {
  const normalizedMethod = (method || '').toUpperCase();
  if (SAFE_METHODS.has(normalizedMethod)) {
    return headers;
  }
  const token = csrfService.getToken();
  const headerName = csrfService.getHeaderName();
  if (!token || !headerName || headers.has(headerName)) {
    return headers;
  }
  return headers.set(headerName, token);
}
