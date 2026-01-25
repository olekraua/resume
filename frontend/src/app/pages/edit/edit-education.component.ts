import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormArray, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { ProfileApiService, EducationItem, ProfileDetails } from '../../services/profile-api.service';
import { EducationPayload, ProfileEditApiService } from '../../services/profile-edit-api.service';
import { SessionService } from '../../services/session.service';
import { StaticDataService } from '../../services/static-data.service';
import { parseApiError } from '../../utils/api-error';

@Component({
  selector: 'app-edit-education',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './edit-education.component.html'
})
export class EditEducationComponent implements OnInit, OnDestroy {
  form = this.fb.group({
    items: this.fb.array([])
  });

  years: number[] = [];
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
      this.years = data.educationYears || [];
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

  addItem(item?: EducationItem): void {
    this.items.push(this.fb.group({
      university: [item?.university || '', Validators.required],
      faculty: [item?.faculty || '', Validators.required],
      summary: [item?.summary || '', Validators.required],
      beginYear: [item?.beginYear ?? null, Validators.required],
      finishYear: [item?.finishYear ?? null],
      ongoing: [item ? !item.finish : false]
    }));
  }

  removeItem(index: number): void {
    this.items.removeAt(index);
  }

  toggleOngoing(index: number): void {
    const group = this.items.at(index);
    if (!group) return;
    const ongoing = group.get('ongoing')?.value;
    if (ongoing) {
      group.get('finishYear')?.setValue(null);
    }
  }

  submit(): void {
    this.errorMessage = '';
    this.successMessage = '';
    const rawItems = this.items.getRawValue() as Array<Record<string, string | number | boolean | null>>;
    const filtered = rawItems.filter((item) => {
      const hasUniversity = Boolean((item.university as string)?.trim());
      const hasFaculty = Boolean((item.faculty as string)?.trim());
      const hasSummary = Boolean((item.summary as string)?.trim());
      return hasUniversity || hasFaculty || hasSummary;
    });

    if (filtered.length === 0) {
      this.errorMessage = 'Add at least one education entry.';
      return;
    }

    const invalid = filtered.some((item) => {
      const hasUniversity = Boolean((item.university as string)?.trim());
      const hasFaculty = Boolean((item.faculty as string)?.trim());
      const hasSummary = Boolean((item.summary as string)?.trim());
      const hasBeginYear = item.beginYear != null;
      return !(hasUniversity && hasFaculty && hasSummary && hasBeginYear);
    });

    if (invalid) {
      this.errorMessage = 'Fill all required fields for each education entry.';
      return;
    }

    const payload: EducationPayload[] = filtered.map((item) => ({
      university: normalizeValue(item.university as string),
      faculty: normalizeValue(item.faculty as string),
      summary: normalizeValue(item.summary as string),
      beginYear: item.beginYear == null ? null : Number(item.beginYear),
      finishYear: item.ongoing ? null : item.finishYear == null ? null : Number(item.finishYear)
    }));

    this.editApi.updateEducation(payload).subscribe({
      next: () => {
        this.successMessage = 'Education updated.';
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
        const items = profile.educations || [];
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
