import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { HobbyItem, ProfileApiService, ProfileDetails } from '../../services/profile-api.service';
import { ProfileEditApiService } from '../../services/profile-edit-api.service';
import { SessionService } from '../../services/session.service';
import { StaticDataService } from '../../services/static-data.service';
import { parseApiError } from '../../utils/api-error';

@Component({
  selector: 'app-edit-hobbies',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './edit-hobbies.component.html'
})
export class EditHobbiesComponent implements OnInit, OnDestroy {
  hobbies: HobbyItem[] = [];
  selectedIds = new Set<number>();

  errorMessage = '';
  successMessage = '';
  maxHobbies = 5;

  private destroyed = new Subject<void>();

  constructor(
    private profileApi: ProfileApiService,
    private editApi: ProfileEditApiService,
    private sessionService: SessionService,
    private staticDataService: StaticDataService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.staticDataService.getStaticData().pipe(takeUntil(this.destroyed)).subscribe((data) => {
      this.hobbies = data.hobbies || [];
    });

    this.sessionService.load().pipe(takeUntil(this.destroyed)).subscribe((session) => {
      if (!session.authenticated || !session.uid) {
        this.router.navigate(['/login']);
        return;
      }
      this.profileApi.getProfile(session.uid).pipe(takeUntil(this.destroyed)).subscribe({
        next: (profile: ProfileDetails) => {
          this.selectedIds = new Set((profile.hobbies || []).map((hobby) => hobby.id));
        }
      });
    });
  }

  ngOnDestroy(): void {
    this.destroyed.next();
    this.destroyed.complete();
  }

  toggleHobby(id: number): void {
    if (this.selectedIds.has(id)) {
      this.selectedIds.delete(id);
      return;
    }
    if (this.selectedIds.size >= this.maxHobbies) {
      this.errorMessage = `You can select up to ${this.maxHobbies} hobbies.`;
      return;
    }
    this.errorMessage = '';
    this.selectedIds.add(id);
  }

  isSelected(id: number): boolean {
    return this.selectedIds.has(id);
  }

  submit(): void {
    this.errorMessage = '';
    this.successMessage = '';
    const ids = Array.from(this.selectedIds);
    if (ids.length === 0) {
      this.errorMessage = 'Select at least one hobby.';
      return;
    }
    this.editApi.updateHobbies({ hobbyIds: ids }).subscribe({
      next: () => {
        this.successMessage = 'Hobbies updated.';
      },
      error: (err) => {
        const parsed = parseApiError(err);
        this.errorMessage = parsed.message;
      }
    });
  }
}
