import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface PageResponse<T> {
  items: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
}

export interface ProfileSummary {
  uid: string;
  fullName: string;
  age: number;
  city: string | null;
  country: string | null;
  objective: string | null;
  summary: string | null;
  smallPhoto: string | null;
}

export interface ContactsItem {
  facebook: string | null;
  linkedin: string | null;
  github: string | null;
  stackoverflow: string | null;
}

export interface SkillItem {
  category: string | null;
  value: string | null;
}

export interface LanguageItem {
  name: string | null;
  level: string | null;
  type: string | null;
  hasLanguageType: boolean;
}

export interface HobbyItem {
  id: number;
  name: string | null;
  cssClassName: string | null;
}

export interface PracticItem {
  company: string | null;
  position: string | null;
  responsibilities: string | null;
  beginDate: string | null;
  finishDate: string | null;
  finish: boolean;
  demo: string | null;
  src: string | null;
}

export interface CertificateItem {
  name: string | null;
  issuer: string | null;
  smallUrl: string | null;
  largeUrl: string | null;
}

export interface CourseItem {
  name: string | null;
  school: string | null;
  finishDate: string | null;
  finish: boolean;
}

export interface EducationItem {
  faculty: string | null;
  summary: string | null;
  university: string | null;
  beginYear: number | null;
  finishYear: number | null;
  finish: boolean;
}

export interface ProfileDetails {
  uid: string;
  firstName: string | null;
  lastName: string | null;
  fullName: string | null;
  age: number;
  birthDay: string | null;
  city: string | null;
  country: string | null;
  objective: string | null;
  summary: string | null;
  info: string | null;
  largePhoto: string | null;
  smallPhoto: string | null;
  phone: string | null;
  email: string | null;
  completed: boolean;
  ownProfile: boolean;
  contacts: ContactsItem | null;
  skills: SkillItem[];
  languages: LanguageItem[];
  hobbies: HobbyItem[];
  practics: PracticItem[];
  certificates: CertificateItem[];
  courses: CourseItem[];
  educations: EducationItem[];
}

export interface StaticDataResponse {
  skillCategories: { id: number; category: string }[];
  languageTypes: { code: string; label: string }[];
  languageLevels: { code: string; sliderValue: number }[];
  hobbies: { id: number; name: string; cssClassName: string }[];
  practicYears: number[];
  courseYears: number[];
  educationYears: number[];
  months: { value: number; label: string }[];
}

@Injectable({
  providedIn: 'root'
})
export class ProfileApiService {
  constructor(private http: HttpClient) {}

  getProfiles(page: number, size?: number): Observable<PageResponse<ProfileSummary>> {
    let params = new HttpParams().set('page', String(page));
    if (size != null) {
      params = params.set('size', String(size));
    }
    return this.http.get<PageResponse<ProfileSummary>>('/api/profiles', { params });
  }

  searchProfiles(query: string, page: number, size?: number): Observable<PageResponse<ProfileSummary>> {
    let params = new HttpParams()
      .set('q', query)
      .set('page', String(page));
    if (size != null) {
      params = params.set('size', String(size));
    }
    return this.http.get<PageResponse<ProfileSummary>>('/api/search', { params });
  }

  getProfile(uid: string): Observable<ProfileDetails> {
    return this.http.get<ProfileDetails>(`/api/profiles/${encodeURIComponent(uid)}`);
  }

  getStaticData(): Observable<StaticDataResponse> {
    return this.http.get<StaticDataResponse>('/api/static-data');
  }
}
