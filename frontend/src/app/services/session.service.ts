import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { catchError, Observable, of, ReplaySubject, tap } from 'rxjs';

export interface SessionInfo {
  authenticated: boolean;
  uid?: string | null;
  fullName?: string | null;
}

@Injectable({
  providedIn: 'root'
})
export class SessionService {
  private sessionSubject = new ReplaySubject<SessionInfo>(1);
  private loaded = false;

  constructor(private http: HttpClient) {}

  load(): Observable<SessionInfo> {
    if (this.loaded) {
      return this.sessionSubject.asObservable();
    }
    this.loaded = true;
    this.http.get('/api/csrf', { responseType: 'text' }).pipe(
      catchError(() => of(''))
    ).subscribe();
    this.http.get<SessionInfo>('/api/me').pipe(
      catchError(() => of({ authenticated: false })),
      tap((info) => this.sessionSubject.next(info))
    ).subscribe();
    return this.sessionSubject.asObservable();
  }

  session$(): Observable<SessionInfo> {
    return this.sessionSubject.asObservable();
  }

  setSession(info: SessionInfo): void {
    this.sessionSubject.next(info);
  }

  clearSession(): void {
    this.sessionSubject.next({ authenticated: false, uid: null, fullName: null });
  }

  refresh(): Observable<SessionInfo> {
    const request = this.http.get<SessionInfo>('/api/me').pipe(
      catchError(() => of({ authenticated: false })),
      tap((info) => this.sessionSubject.next(info))
    );
    return request;
  }
}
