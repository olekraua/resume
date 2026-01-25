import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormArray, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { ProfileApiService, ProfileDetails } from '../../services/profile-api.service';
import { ProfileEditApiService, SkillPayload } from '../../services/profile-edit-api.service';
import { SessionService } from '../../services/session.service';
import { StaticDataService } from '../../services/static-data.service';
import { parseApiError } from '../../utils/api-error';

@Component({
  selector: 'app-edit-skills',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './edit-skills.component.html'
})
export class EditSkillsComponent implements OnInit, OnDestroy {
  form = this.fb.group({
    items: this.fb.array([])
  });

  categories: string[] = [];
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
      this.categories = (data.skillCategories || []).map((item) => item.category).filter(Boolean);
      if (this.categories.length === 0) {
        this.categories = ['Languages', 'DBMS', 'Web', 'Java', 'IDE', 'CVS', 'Web Servers', 'Build system',
          'Cloud', 'Frameworks', 'Tools', 'Testing', 'Other'];
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

  addItem(skill?: SkillPayload): void {
    this.items.push(this.fb.group({
      category: [skill?.category || '', Validators.required],
      value: [skill?.value || '', Validators.required]
    }));
  }

  removeItem(index: number): void {
    this.items.removeAt(index);
  }

  submit(): void {
    this.errorMessage = '';
    this.successMessage = '';
    const rawItems = this.items.getRawValue() as SkillPayload[];
    const filtered = rawItems.filter((item) => {
      const hasCategory = Boolean(item.category && item.category.toString().trim());
      const hasValue = Boolean(item.value && item.value.toString().trim());
      return hasCategory || hasValue;
    }).map((item) => ({
      category: item.category ? item.category.toString().trim() : null,
      value: item.value ? item.value.toString().trim() : null
    }));

    if (filtered.length === 0) {
      this.errorMessage = 'Add at least one skill.';
      return;
    }

    this.editApi.updateSkills(filtered).subscribe({
      next: () => {
        this.successMessage = 'Skills updated.';
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
        const skills = profile.skills || [];
        if (skills.length === 0) {
          this.addItem();
          return;
        }
        skills.forEach((skill) => this.addItem(skill));
      },
      error: () => {
        this.items.clear();
        this.addItem();
      }
    });
  }
}
