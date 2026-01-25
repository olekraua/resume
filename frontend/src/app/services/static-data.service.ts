import { Injectable } from '@angular/core';
import { catchError, map, Observable, of, shareReplay } from 'rxjs';
import { ProfileApiService, StaticDataResponse } from './profile-api.service';

@Injectable({
  providedIn: 'root'
})
export class StaticDataService {
  private languageTypeLabels$?: Observable<Map<string, string>>;
  private staticData$?: Observable<StaticDataResponse>;

  constructor(private api: ProfileApiService) {}

  getStaticData(): Observable<StaticDataResponse> {
    if (!this.staticData$) {
      const empty: StaticDataResponse = {
        skillCategories: [],
        languageTypes: [],
        languageLevels: [],
        hobbies: [],
        practicYears: [],
        courseYears: [],
        educationYears: [],
        months: []
      };
      this.staticData$ = this.api.getStaticData().pipe(
        catchError(() => of(empty)),
        shareReplay(1)
      );
    }
    return this.staticData$;
  }

  getLanguageTypeLabels(): Observable<Map<string, string>> {
    if (!this.languageTypeLabels$) {
      this.languageTypeLabels$ = this.getStaticData().pipe(
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
