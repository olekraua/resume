import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { SessionInfo } from './session.service';

export interface LoginRequest {
  username: string;
  password: string;
  rememberMe: boolean;
}

export interface RegistrationRequest {
  uid: string;
  firstName: string;
  lastName: string;
  password: string;
  confirmPassword: string;
}

export interface RestoreRequest {
  identifier: string;
}

export interface RestoreRequestResponse {
  requested: boolean;
  restoreLink?: string | null;
}

export interface RestoreTokenResponse {
  valid: boolean;
}

export interface RestorePasswordRequest {
  password: string;
  confirmPassword: string;
}

export interface UidHintResponse {
  uid: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthApiService {
  constructor(private http: HttpClient) {}

  login(payload: LoginRequest): Observable<SessionInfo> {
    return this.http.post<SessionInfo>('/api/auth/login', payload);
  }

  logout(): Observable<void> {
    return this.http.post<void>('/api/auth/logout', {});
  }

  register(payload: RegistrationRequest): Observable<SessionInfo> {
    return this.http.post<SessionInfo>('/api/auth/register', payload);
  }

  requestUidHint(firstName?: string | null, lastName?: string | null): Observable<UidHintResponse> {
    let params = new HttpParams();
    if (firstName != null) {
      params = params.set('firstName', firstName);
    }
    if (lastName != null) {
      params = params.set('lastName', lastName);
    }
    return this.http.get<UidHintResponse>('/api/auth/uid-hint', { params });
  }

  requestRestore(payload: RestoreRequest): Observable<RestoreRequestResponse> {
    return this.http.post<RestoreRequestResponse>('/api/auth/restore', payload);
  }

  checkRestoreToken(token: string): Observable<RestoreTokenResponse> {
    return this.http.get<RestoreTokenResponse>(`/api/auth/restore/${encodeURIComponent(token)}`);
  }

  resetPassword(token: string, payload: RestorePasswordRequest): Observable<void> {
    return this.http.post<void>(`/api/auth/restore/${encodeURIComponent(token)}`, payload);
  }
}
