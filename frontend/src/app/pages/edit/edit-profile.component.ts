import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { Subject, forkJoin, takeUntil } from 'rxjs';
import { ProfileApiService, ProfileDetails } from '../../services/profile-api.service';
import { ProfileEditApiService } from '../../services/profile-edit-api.service';
import { SessionService } from '../../services/session.service';
import { parseApiError } from '../../utils/api-error';

@Component({
  selector: 'app-edit-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './edit-profile.component.html'
})
export class EditProfileComponent implements OnInit, OnDestroy {
  form = this.fb.group({
    birthDay: [''],
    country: [''],
    city: [''],
    email: ['', [Validators.email]],
    phone: [''],
    objective: ['', Validators.required],
    summary: ['', Validators.required],
    info: [''],
    facebook: [''],
    linkedin: [''],
    github: [''],
    stackoverflow: ['']
  });

  profile?: ProfileDetails;
  loading = true;
  saving = false;
  successMessage = '';
  errorMessage = '';

  selectedPhoto: File | null = null;
  photoPreviewUrl: string | null = null;

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
    this.revokePreview();
  }

  get photoUrl(): string {
    if (this.photoPreviewUrl) {
      return this.photoPreviewUrl;
    }
    return this.profile?.largePhoto || '/assets/img/profile-placeholder.png';
  }

  onPhotoSelected(event: Event): void {
    const input = event.target as HTMLInputElement | null;
    const file = input?.files && input.files.length > 0 ? input.files[0] : null;
    if (!file) {
      return;
    }
    this.selectedPhoto = file;
    this.revokePreview();
    this.photoPreviewUrl = URL.createObjectURL(file);
  }

  uploadPhoto(): void {
    if (!this.selectedPhoto) {
      this.errorMessage = 'Select a photo to upload.';
      return;
    }
    this.errorMessage = '';
    this.successMessage = '';
    const file = this.selectedPhoto;
    this.editApi.uploadPhoto(file).subscribe({
      next: (response) => {
        if (this.profile) {
          this.profile.largePhoto = response.largeUrl;
          this.profile.smallPhoto = response.smallUrl;
        }
        this.selectedPhoto = null;
        this.revokePreview();
        this.successMessage = 'Photo updated.';
      },
      error: (err) => {
        const parsed = parseApiError(err);
        this.errorMessage = parsed.message;
      }
    });
  }

  removePhoto(): void {
    this.errorMessage = '';
    this.successMessage = '';
    this.editApi.removePhoto().subscribe({
      next: () => {
        if (this.profile) {
          this.profile.largePhoto = null;
          this.profile.smallPhoto = null;
        }
        this.selectedPhoto = null;
        this.revokePreview();
        this.successMessage = 'Photo removed.';
      },
      error: (err) => {
        const parsed = parseApiError(err);
        this.errorMessage = parsed.message;
      }
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.errorMessage = '';
    this.successMessage = '';
    this.saving = true;

    const raw = this.form.getRawValue();
    const infoPayload = {
      birthDay: normalizeValue(raw.birthDay),
      country: normalizeValue(raw.country),
      city: normalizeValue(raw.city),
      objective: normalizeValue(raw.objective),
      summary: normalizeValue(raw.summary),
      info: normalizeValue(raw.info)
    };
    const contactsPayload = {
      phone: normalizeValue(raw.phone),
      email: normalizeValue(raw.email),
      facebook: normalizeValue(raw.facebook),
      linkedin: normalizeValue(raw.linkedin),
      github: normalizeValue(raw.github),
      stackoverflow: normalizeValue(raw.stackoverflow)
    };

    forkJoin([
      this.editApi.updateInfo(infoPayload),
      this.editApi.updateContacts(contactsPayload)
    ]).subscribe({
      next: () => {
        this.saving = false;
        this.successMessage = 'Profile updated.';
      },
      error: (err) => {
        this.saving = false;
        const parsed = parseApiError(err);
        this.errorMessage = parsed.message;
      }
    });
  }

  private loadProfile(uid: string): void {
    this.loading = true;
    this.profileApi.getProfile(uid).pipe(takeUntil(this.destroyed)).subscribe({
      next: (profile) => {
        this.profile = profile;
        this.form.patchValue({
          birthDay: profile.birthDay || '',
          country: profile.country || '',
          city: profile.city || '',
          email: profile.email || '',
          phone: profile.phone || '',
          objective: profile.objective || '',
          summary: profile.summary || '',
          info: profile.info || '',
          facebook: profile.contacts?.facebook || '',
          linkedin: profile.contacts?.linkedin || '',
          github: profile.contacts?.github || '',
          stackoverflow: profile.contacts?.stackoverflow || ''
        });
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  private revokePreview(): void {
    if (this.photoPreviewUrl) {
      URL.revokeObjectURL(this.photoPreviewUrl);
      this.photoPreviewUrl = null;
    }
  }
}

function normalizeValue(value: string | null | undefined): string | null {
  if (value == null) {
    return null;
  }
  const trimmed = value.toString().trim();
  return trimmed ? trimmed : null;
}
