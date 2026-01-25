import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormArray, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { LanguageItem, ProfileApiService, ProfileDetails } from '../../services/profile-api.service';
import { LanguagePayload, ProfileEditApiService } from '../../services/profile-edit-api.service';
import { SessionService } from '../../services/session.service';
import { StaticDataService } from '../../services/static-data.service';
import { parseApiError } from '../../utils/api-error';

interface LanguageTypeOption {
  code: string;
  label: string;
}

interface LanguageLevelOption {
  code: string;
  sliderValue: number;
}

@Component({
  selector: 'app-edit-languages',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './edit-languages.component.html'
})
export class EditLanguagesComponent implements OnInit, OnDestroy {
  form = this.fb.group({
    items: this.fb.array([])
  });

  languageTypes: LanguageTypeOption[] = [];
  languageLevels: LanguageLevelOption[] = [];

  errorMessage = '';
  successMessage = '';

  private destroyed = new Subject<void>();

  constructor(
    private fb: FormBuilder,
    private profileApi: ProfileApiService,
    private editApi: ProfileEditApiService,
    private sessionService: SessionService,
    private staticDataService: StaticDataService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.staticDataService.getStaticData().pipe(takeUntil(this.destroyed)).subscribe((data) => {
      this.languageTypes = data.languageTypes || [];
      this.languageLevels = data.languageLevels || [];
      if (this.languageTypes.length === 0) {
        this.languageTypes = [
          { code: 'ALL', label: 'All' },
          { code: 'SPOKEN', label: 'Spoken' },
          { code: 'WRITING', label: 'Writing' }
        ];
      }
      if (this.languageLevels.length === 0) {
        this.languageLevels = [
          { code: 'BEGINNER', sliderValue: 0 },
          { code: 'ELEMENTARY', sliderValue: 1 },
          { code: 'PRE_INTERMEDIATE', sliderValue: 2 },
          { code: 'INTERMEDIATE', sliderValue: 3 },
          { code: 'UPPER_INTERMEDIATE', sliderValue: 4 },
          { code: 'ADVANCED', sliderValue: 5 },
          { code: 'PROFICIENCY', sliderValue: 6 }
        ];
      }
    });

    this.sessionService.load().pipe(takeUntil(this.destroyed)).subscribe((session) => {
      if (!session.authenticated || !session.uid) {
        this.router.navigate(['/login']);
        return;
      }
      this.loadProfile(session.uid);
    });
  }

  ngOnDestroy(): void {
    this.destroyed.next();
    this.destroyed.complete();
  }

  get items(): FormArray {
    return this.form.get('items') as FormArray;
  }

  addItem(item?: LanguageItem): void {
    this.items.push(this.fb.group({
      name: [item?.name || '', Validators.required],
      level: [item?.level || '', Validators.required],
      type: [item?.type || 'ALL']
    }));
  }

  removeItem(index: number): void {
    this.items.removeAt(index);
  }

  submit(): void {
    this.errorMessage = '';
    this.successMessage = '';
    const rawItems = this.items.getRawValue() as Array<Record<string, string>>;
    const filtered = rawItems.filter((item) => {
      const hasName = Boolean(item.name?.trim());
      return hasName;
    });

    if (filtered.length === 0) {
      this.errorMessage = 'Add at least one language.';
      return;
    }

    const invalid = filtered.some((item) => {
      const hasName = Boolean(item.name?.trim());
      const hasLevel = Boolean(item.level?.trim());
      return !(hasName && hasLevel);
    });

    if (invalid) {
      this.errorMessage = 'Each language requires a name and level.';
      return;
    }

    const payload: LanguagePayload[] = filtered.map((item) => ({
      name: normalizeValue(item.name),
      level: normalizeValue(item.level),
      type: item.type ? item.type : 'ALL'
    }));

    this.editApi.updateLanguages(payload).subscribe({
      next: () => {
        this.successMessage = 'Languages updated.';
      },
      error: (err) => {
        const parsed = parseApiError(err);
        this.errorMessage = parsed.message;
      }
    });
  }

  private loadProfile(uid: string): void {
    this.profileApi.getProfile(uid).pipe(takeUntil(this.destroyed)).subscribe({
      next: (profile: ProfileDetails) => {
        this.items.clear();
        const items = profile.languages || [];
        if (items.length === 0) {
          this.addItem();
          return;
        }
        items.forEach((item) => this.addItem(item));
      },
      error: () => {
        this.items.clear();
        this.addItem();
      }
    });
  }
}

function normalizeValue(value: string | null | undefined): string | null {
  if (value == null) {
    return null;
  }
  const trimmed = value.toString().trim();
  return trimmed ? trimmed : null;
}
