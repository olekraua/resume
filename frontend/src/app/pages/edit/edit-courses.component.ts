import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormArray, FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { CourseItem, ProfileApiService, ProfileDetails } from '../../services/profile-api.service';
import { CoursePayload, ProfileEditApiService } from '../../services/profile-edit-api.service';
import { SessionService } from '../../services/session.service';
import { parseApiError } from '../../utils/api-error';

@Component({
  selector: 'app-edit-courses',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './edit-courses.component.html'
})
export class EditCoursesComponent implements OnInit, OnDestroy {
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

  addItem(item?: CourseItem): void {
    this.items.push(this.fb.group({
      name: [item?.name || ''],
      school: [item?.school || ''],
      finishDate: [item?.finishDate || ''],
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
      const hasName = Boolean((item.name as string)?.trim());
      const hasSchool = Boolean((item.school as string)?.trim());
      const hasFinish = Boolean((item.finishDate as string)?.trim());
      return hasName || hasSchool || hasFinish;
    });

    if (filtered.length === 0) {
      this.errorMessage = 'Add at least one course.';
      return;
    }

    const payload: CoursePayload[] = filtered.map((item) => ({
      name: normalizeValue(item.name as string),
      school: normalizeValue(item.school as string),
      finishDate: item.ongoing ? null : normalizeValue(item.finishDate as string)
    }));

    this.editApi.updateCourses(payload).subscribe({
      next: () => {
        this.successMessage = 'Courses updated.';
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
        const items = profile.courses || [];
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
