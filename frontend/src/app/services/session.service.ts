import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, catchError, Observable, of, shareReplay, tap } from 'rxjs';

export interface SessionInfo {
  authenticated: boolean;
  uid?: string | null;
  fullName?: string | null;
}

@Injectable({
  providedIn: 'root'
})
export class SessionService {
  private sessionSubject = new BehaviorSubject<SessionInfo>({ authenticated: false });
  private loaded = false;

  constructor(private http: HttpClient) {}

  load(): Observable<SessionInfo> {
    if (this.loaded) {
      return this.sessionSubject.asObservable();
    }
    this.loaded = true;
    const request = this.http.get<SessionInfo>('/api/me').pipe(
      catchError(() => of({ authenticated: false })),
      tap((info) => this.sessionSubject.next(info)),
      shareReplay(1)
    );
    request.subscribe();
    return this.sessionSubject.asObservable();
  }

  session$(): Observable<SessionInfo> {
    return this.sessionSubject.asObservable();
  }
}
