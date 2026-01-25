import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

export interface ChangeLoginRequest {
  newUid: string;
}

export interface ChangeLoginResponse {
  newUid: string;
  reloginRequired: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class AccountApiService {
  constructor(private http: HttpClient) {}

  changePassword(payload: ChangePasswordRequest): Observable<void> {
    return this.http.post<void>('/api/account/password', payload);
  }

  changeLogin(payload: ChangeLoginRequest): Observable<ChangeLoginResponse> {
    return this.http.post<ChangeLoginResponse>('/api/account/login', payload);
  }

  removeAccount(): Observable<void> {
    return this.http.delete<void>('/api/account/remove');
  }
}
