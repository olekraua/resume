import { ApplicationConfig } from '@angular/core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideRouter } from '@angular/router';

import { environment } from '../environments/environment';
import { routes } from './app.routes';
import { API_BASE_URL } from './config/api-config';
import { apiBaseInterceptor } from './interceptors/api-base.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(withInterceptors([apiBaseInterceptor])),
    { provide: API_BASE_URL, useValue: environment.apiBaseUrl }
  ]
};
