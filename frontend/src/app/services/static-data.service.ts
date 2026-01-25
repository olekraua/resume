import { Injectable } from '@angular/core';
import { catchError, map, Observable, of, shareReplay } from 'rxjs';
import { ProfileApiService } from './profile-api.service';

@Injectable({
  providedIn: 'root'
})
export class StaticDataService {
  private languageTypeLabels$?: Observable<Map<string, string>>;

  constructor(private api: ProfileApiService) {}

  getLanguageTypeLabels(): Observable<Map<string, string>> {
    if (!this.languageTypeLabels$) {
      this.languageTypeLabels$ = this.api.getStaticData().pipe(
        map((data) => {
          const mapValues = new Map<string, string>();
          const types = data?.languageTypes || [];
          types.forEach((item) => {
            if (!item || !item.code) return;
            mapValues.set(item.code, item.label || item.code);
          });
          return mapValues;
        }),
        catchError(() => of(new Map<string, string>())),
        shareReplay(1)
      );
    }
    return this.languageTypeLabels$;
  }
}
