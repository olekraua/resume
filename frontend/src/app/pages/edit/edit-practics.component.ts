import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormArray, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { ProfileApiService, ProfileDetails, PracticItem } from '../../services/profile-api.service';
import { PracticPayload, ProfileEditApiService } from '../../services/profile-edit-api.service';
import { SessionService } from '../../services/session.service';
import { parseApiError } from '../../utils/api-error';

@Component({
  selector: 'app-edit-practics',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './edit-practics.component.html'
})
export class EditPracticsComponent implements OnInit, OnDestroy {
  form = this.fb.group({
    items: this.fb.array([])
  });

  errorMessage = '';
  successMessage = '';

  private destroyed = new Subject<void>();

  constructor(
    private fb: FormBuilder,
    private profileApi: ProfileApiService,
    private editApi: ProfileEditApiService,
    private sessionService: SessionService,
    private router: Router
  ) {}

  ngOnInit(): void {
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

  addItem(item?: PracticItem): void {
    this.items.push(this.fb.group({
      position: [item?.position || '', Validators.required],
      company: [item?.company || '', Validators.required],
      beginDate: [item?.beginDate || '', Validators.required],
      finishDate: [item?.finishDate || ''],
      responsibilities: [item?.responsibilities || '', Validators.required],
      demo: [item?.demo || ''],
      src: [item?.src || ''],
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
      group.get('finishDate')?.setValue('');
    }
  }

  submit(): void {
    this.errorMessage = '';
    this.successMessage = '';
    const rawItems = this.items.getRawValue() as Array<Record<string, string | boolean>>;
    const filtered = rawItems.filter((item) => {
      const hasPosition = Boolean((item.position as string)?.trim());
      const hasCompany = Boolean((item.company as string)?.trim());
      const hasBegin = Boolean((item.beginDate as string)?.trim());
      const hasResponsibilities = Boolean((item.responsibilities as string)?.trim());
      return hasPosition || hasCompany || hasBegin || hasResponsibilities;
    });

    if (filtered.length === 0) {
      this.errorMessage = 'Add at least one practics entry.';
      return;
    }

    const invalid = filtered.some((item) => {
      const hasPosition = Boolean((item.position as string)?.trim());
      const hasCompany = Boolean((item.company as string)?.trim());
      const hasBegin = Boolean((item.beginDate as string)?.trim());
      const hasResponsibilities = Boolean((item.responsibilities as string)?.trim());
      return !(hasPosition && hasCompany && hasBegin && hasResponsibilities);
    });

    if (invalid) {
      this.errorMessage = 'Fill position, company, start date, and responsibilities for each entry.';
      return;
    }

    const payload: PracticPayload[] = filtered.map((item) => ({
      position: normalizeValue(item.position as string),
      company: normalizeValue(item.company as string),
      beginDate: normalizeValue(item.beginDate as string),
      finishDate: item.ongoing ? null : normalizeValue(item.finishDate as string),
      responsibilities: normalizeValue(item.responsibilities as string),
      demo: normalizeValue(item.demo as string),
      src: normalizeValue(item.src as string)
    }));

    this.editApi.updatePractics(payload).subscribe({
      next: () => {
        this.successMessage = 'Practics updated.';
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
        const items = profile.practics || [];
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
