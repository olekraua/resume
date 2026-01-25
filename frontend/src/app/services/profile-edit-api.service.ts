import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface InfoPayload {
  birthDay: string | null;
  country: string | null;
  city: string | null;
  objective: string | null;
  summary: string | null;
  info: string | null;
}

export interface ContactsPayload {
  phone: string | null;
  email: string | null;
  facebook: string | null;
  linkedin: string | null;
  github: string | null;
  stackoverflow: string | null;
}

export interface SkillPayload {
  category: string | null;
  value: string | null;
}

export interface PracticPayload {
  company: string | null;
  position: string | null;
  responsibilities: string | null;
  beginDate: string | null;
  finishDate: string | null;
  demo: string | null;
  src: string | null;
}

export interface EducationPayload {
  faculty: string | null;
  summary: string | null;
  university: string | null;
  beginYear: number | null;
  finishYear: number | null;
}

export interface CoursePayload {
  name: string | null;
  school: string | null;
  finishDate: string | null;
}

export interface LanguagePayload {
  name: string | null;
  level: string | null;
  type: string | null;
}

export interface CertificatePayload {
  name: string | null;
  issuer: string | null;
  smallUrl: string | null;
  largeUrl: string | null;
}

export interface HobbyPayload {
  hobbyIds: number[];
}

export interface PhotoResponse {
  largeUrl: string;
  smallUrl: string;
}

export interface UploadCertificateResponse {
  certificateName: string | null;
  largeUrl: string | null;
  smallUrl: string | null;
}

@Injectable({
  providedIn: 'root'
})
export class ProfileEditApiService {
  constructor(private http: HttpClient) {}

  updateInfo(payload: InfoPayload): Observable<void> {
    return this.http.put<void>('/api/profile/info', payload);
  }

  updateContacts(payload: ContactsPayload): Observable<void> {
    return this.http.put<void>('/api/profile/contacts', payload);
  }

  updateSkills(items: SkillPayload[]): Observable<void> {
    return this.http.put<void>('/api/profile/skills', { items });
  }

  updatePractics(items: PracticPayload[]): Observable<void> {
    return this.http.put<void>('/api/profile/practics', { items });
  }

  updateEducation(items: EducationPayload[]): Observable<void> {
    return this.http.put<void>('/api/profile/education', { items });
  }

  updateCourses(items: CoursePayload[]): Observable<void> {
    return this.http.put<void>('/api/profile/courses', { items });
  }

  updateLanguages(items: LanguagePayload[]): Observable<void> {
    return this.http.put<void>('/api/profile/languages', { items });
  }

  updateCertificates(items: CertificatePayload[]): Observable<void> {
    return this.http.put<void>('/api/profile/certificates', { items });
  }

  updateHobbies(payload: HobbyPayload): Observable<void> {
    return this.http.put<void>('/api/profile/hobbies', payload);
  }

  updatePassword(payload: { currentPassword: string; newPassword: string; confirmPassword: string }): Observable<void> {
    return this.http.put<void>('/api/profile/password', payload);
  }

  uploadPhoto(file: File): Observable<PhotoResponse> {
    const formData = new FormData();
    formData.append('profilePhoto', file);
    return this.http.post<PhotoResponse>('/api/profile/photo', formData);
  }

  removePhoto(): Observable<void> {
    return this.http.delete<void>('/api/profile/photo');
  }

  uploadCertificate(file: File): Observable<UploadCertificateResponse> {
    const formData = new FormData();
    formData.append('certificateFile', file);
    return this.http.post<UploadCertificateResponse>('/api/profile/certificates/upload', formData);
  }
}
