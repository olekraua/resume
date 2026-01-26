import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import {
  CertificateItem,
  ContactsItem,
  EducationItem,
  LanguageItem,
  PracticItem,
  ProfileApiService,
  ProfileDetails,
  SkillItem,
  CourseItem,
  HobbyItem
} from '../../services/profile-api.service';
import { StaticDataService } from '../../services/static-data.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './profile.component.html'
})
export class ProfileComponent implements OnInit, OnDestroy {
  profile?: ProfileDetails;
  loading = false;
  languageTypeLabels = new Map<string, string>();

  private destroyed = new Subject<void>();

  private fullDateFormatter: Intl.DateTimeFormat;
  private monthYearFormatter: Intl.DateTimeFormat;

  constructor(
    private api: ProfileApiService,
    private staticData: StaticDataService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    const locale = document.documentElement.lang || window.navigator.language || 'en';
    this.fullDateFormatter = new Intl.DateTimeFormat(locale, {
      month: 'short',
      day: '2-digit',
      year: 'numeric',
      timeZone: 'UTC'
    });
    this.monthYearFormatter = new Intl.DateTimeFormat(locale, {
      month: 'short',
      year: 'numeric',
      timeZone: 'UTC'
    });
  }

  ngOnInit(): void {
    this.route.paramMap.pipe(takeUntil(this.destroyed)).subscribe((params) => {
      const uid = (params.get('uid') || '').trim();
      if (!uid) {
        this.router.navigate(['/welcome']);
        return;
      }
      this.loadProfile(uid);
    });
  }

  ngOnDestroy(): void {
    this.destroyed.next();
    this.destroyed.complete();
  }

  get fullName(): string {
    if (!this.profile) return '';
    const value = (this.profile.fullName || '').trim();
    if (value) return value;
    const parts: string[] = [];
    if (this.profile.firstName) parts.push(this.profile.firstName);
    if (this.profile.lastName) parts.push(this.profile.lastName);
    return parts.join(' ').trim();
  }

  get location(): string {
    if (!this.profile) return '';
    const parts: string[] = [];
    if (this.profile.city) parts.push(this.profile.city);
    if (this.profile.country) parts.push(this.profile.country);
    return parts.join(', ');
  }

  get hasContactLinks(): boolean {
    const contacts = this.profile?.contacts;
    return Boolean(
      this.profile?.phone ||
      this.profile?.email ||
      contacts?.facebook ||
      contacts?.linkedin ||
      contacts?.github ||
      contacts?.stackoverflow
    );
  }

  get photoLarge(): string {
    return this.profile?.largePhoto || '/assets/img/profile-placeholder.png';
  }

  get profileUid(): string {
    return this.profile?.uid || '';
  }

  showLanguageType(language: LanguageItem): string {
    if (!language || !language.hasLanguageType || !language.type) return '';
    return this.languageTypeLabels.get(language.type) || language.type;
  }

  formatFullDate(value: string | null): string {
    const date = this.parseIsoDate(value);
    return date ? this.fullDateFormatter.format(date) : '';
  }

  formatMonthYear(value: string | null): string {
    const date = this.parseIsoDate(value);
    return date ? this.monthYearFormatter.format(date) : '';
  }

  private parseIsoDate(value: string | null): Date | null {
    if (!value) return null;
    const parts = value.split('-');
    if (parts.length < 2) return null;
    const year = Number.parseInt(parts[0], 10);
    const month = Number.parseInt(parts[1], 10);
    const day = Number.parseInt(parts[2] || '1', 10);
    if (!Number.isFinite(year) || !Number.isFinite(month)) return null;
    const safeDay = Number.isFinite(day) ? day : 1;
    return new Date(Date.UTC(year, month - 1, safeDay));
  }

  private loadProfile(uid: string): void {
    this.profile = undefined;
    this.loading = true;
    this.api.getProfile(uid).pipe(takeUntil(this.destroyed)).subscribe({
      next: (profile) => {
        this.profile = profile;
        this.loading = false;
        document.title = this.fullName || 'My Resume';
        this.resolveLanguageLabels(profile.languages || []);
        this.initCertificateViewer();
      },
      error: () => {
        this.loading = false;
        window.location.href = `/${encodeURIComponent(uid)}`;
      }
    });
  }

  private resolveLanguageLabels(languages: LanguageItem[]): void {
    const needsLabels = languages.some((language) => language && language.hasLanguageType && language.type);
    if (!needsLabels) return;
    this.staticData.getLanguageTypeLabels().pipe(takeUntil(this.destroyed)).subscribe((labels) => {
      this.languageTypeLabels = labels;
    });
  }

  private initCertificateViewer(): void {
    const resume = (window as unknown as { resume?: { initCertificateViewer?: () => void } }).resume;
    if (resume && typeof resume.initCertificateViewer === 'function') {
      resume.initCertificateViewer();
    }
  }

  trackSkill(index: number, item: SkillItem): string {
    return `${index}-${item.category}-${item.value}`;
  }

  trackPractic(index: number, item: PracticItem): string {
    return `${index}-${item.company}-${item.position}`;
  }

  trackCertificate(index: number, item: CertificateItem): string {
    return `${index}-${item.name}-${item.issuer}`;
  }

  trackCourse(index: number, item: CourseItem): string {
    return `${index}-${item.name}-${item.school}`;
  }

  trackEducation(index: number, item: EducationItem): string {
    return `${index}-${item.summary}-${item.university}`;
  }

  trackHobby(index: number, item: HobbyItem): string {
    return `${index}-${item.id}-${item.name}`;
  }

  trackLanguage(index: number, item: LanguageItem): string {
    return `${index}-${item.name}-${item.level}`;
  }

  contactItems(contacts: ContactsItem | null): { icon: string; href: string; label: string; external: boolean }[] {
    if (!this.profile) return [];
    const items: { icon: string; href: string; label: string; external: boolean }[] = [];
    if (this.profile.phone) {
      items.push({ icon: 'fa-phone', href: `tel:${this.profile.phone}`, label: this.profile.phone, external: false });
    }
    if (this.profile.email) {
      items.push({ icon: 'fa-envelope', href: `mailto:${this.profile.email}`, label: this.profile.email, external: false });
    }
    if (contacts?.facebook) {
      items.push({ icon: 'fa-facebook', href: contacts.facebook, label: contacts.facebook, external: true });
    }
    if (contacts?.linkedin) {
      items.push({ icon: 'fa-linkedin', href: contacts.linkedin, label: contacts.linkedin, external: true });
    }
    if (contacts?.github) {
      items.push({ icon: 'fa-github', href: contacts.github, label: contacts.github, external: true });
    }
    if (contacts?.stackoverflow) {
      items.push({ icon: 'fa-stack-overflow', href: contacts.stackoverflow, label: contacts.stackoverflow, external: true });
    }
    return items;
  }

  formatPracticTitle(item: PracticItem): string {
    const position = item.position || '';
    const company = item.company || '';
    if (position && company) return `${position} at ${company}`;
    return position || company;
  }

  formatCourseTitle(item: CourseItem): string {
    const name = item.name || '';
    const school = item.school || '';
    if (name && school) return `${name} at ${school}`;
    return name || school;
  }

  formatEducationTitle(item: EducationItem): string {
    return item.summary || '';
  }

  formatEducationBody(item: EducationItem): string {
    const faculty = item.faculty || '';
    const university = item.university || '';
    if (faculty && university) return `${faculty}, ${university}`;
    return faculty || university;
  }
}
