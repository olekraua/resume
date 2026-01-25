import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormArray, FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { CertificateItem, ProfileApiService, ProfileDetails } from '../../services/profile-api.service';
import { CertificatePayload, ProfileEditApiService } from '../../services/profile-edit-api.service';
import { SessionService } from '../../services/session.service';
import { parseApiError } from '../../utils/api-error';

interface CertificateFormItem {
  name: string | null;
  issuer: string | null;
  smallUrl: string | null;
  largeUrl: string | null;
}

@Component({
  selector: 'app-edit-certificates',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './edit-certificates.component.html'
})
export class EditCertificatesComponent implements OnInit, OnDestroy {
  form = this.fb.group({
    items: this.fb.array([])
  });

  uploading = false;
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

  addItem(item?: CertificateItem): void {
    this.items.push(this.fb.group({
      name: [item?.name || ''],
      issuer: [item?.issuer || ''],
      smallUrl: [item?.smallUrl || ''],
      largeUrl: [item?.largeUrl || '']
    }));
  }

  removeItem(index: number): void {
    this.items.removeAt(index);
  }

  onUploadSelected(event: Event): void {
    const input = event.target as HTMLInputElement | null;
    const file = input?.files && input.files.length > 0 ? input.files[0] : null;
    if (!file) {
      return;
    }
    this.uploading = true;
    this.errorMessage = '';
    this.editApi.uploadCertificate(file).subscribe({
      next: (response) => {
        this.uploading = false;
        this.addItem({
          name: response.certificateName || '',
          issuer: '',
          smallUrl: response.smallUrl || '',
          largeUrl: response.largeUrl || ''
        });
      },
      error: (err) => {
        this.uploading = false;
        const parsed = parseApiError(err);
        this.errorMessage = parsed.message;
      }
    });
  }

  submit(): void {
    this.errorMessage = '';
    this.successMessage = '';
    const rawItems = this.items.getRawValue() as CertificateFormItem[];
    const filtered = rawItems.filter((item) => {
      const hasName = Boolean(item.name?.trim());
      const hasIssuer = Boolean(item.issuer?.trim());
      const hasSmall = Boolean(item.smallUrl?.trim());
      const hasLarge = Boolean(item.largeUrl?.trim());
      return hasName || hasIssuer || hasSmall || hasLarge;
    });

    const invalid = filtered.some((item) => {
      const hasName = Boolean(item.name?.trim());
      const hasIssuer = Boolean(item.issuer?.trim());
      const hasSmall = Boolean(item.smallUrl?.trim());
      const hasLarge = Boolean(item.largeUrl?.trim());
      return !(hasName && hasIssuer && hasSmall && hasLarge);
    });

    if (invalid) {
      this.errorMessage = 'Each certificate must have name, issuer, and image URLs.';
      return;
    }

    const payload: CertificatePayload[] = filtered.map((item) => ({
      name: normalizeValue(item.name),
      issuer: normalizeValue(item.issuer),
      smallUrl: normalizeValue(item.smallUrl),
      largeUrl: normalizeValue(item.largeUrl)
    }));

    this.editApi.updateCertificates(payload).subscribe({
      next: () => {
        this.successMessage = 'Certificates updated.';
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
        const items = profile.certificates || [];
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
