import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthApiService, RegistrationRequest, UidHintResponse } from '../../services/auth-api.service';
import { SessionService } from '../../services/session.service';
import { ApiErrorResponse, parseApiError } from '../../utils/api-error';

interface UidConflictResponse {
  error: ApiErrorResponse;
  uidSuggestions?: string[];
}

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './register.component.html'
})
export class RegisterComponent {
  form = this.fb.group({
    uid: ['', [Validators.required, Validators.minLength(3)]],
    firstName: ['', Validators.required],
    lastName: ['', Validators.required],
    password: ['', [Validators.required, Validators.minLength(6)]],
    confirmPassword: ['', [Validators.required, Validators.minLength(6)]]
  });

  errorMessage = '';
  fieldErrors: Record<string, string[]> = {};
  uidSuggestions: string[] = [];
  loadingHint = false;

  constructor(
    private fb: FormBuilder,
    private authApi: AuthApiService,
    private sessionService: SessionService,
    private router: Router
  ) {}

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.errorMessage = '';
    this.fieldErrors = {};
    this.uidSuggestions = [];
    const payload = this.form.getRawValue() as RegistrationRequest;
    this.authApi.register(payload).subscribe({
      next: (session) => {
        this.sessionService.setSession(session);
        if (session.uid) {
          this.router.navigate(['/', session.uid]);
        } else {
          this.router.navigate(['/welcome']);
        }
      },
      error: (err) => {
        const payload = err?.error as UidConflictResponse | undefined;
        if (payload && payload.error) {
          const apiError = payload.error;
          this.errorMessage = apiError.message || 'Request failed';
          if (Array.isArray(apiError.errors)) {
            apiError.errors.forEach((fieldError) => {
              const key = fieldError.field || 'form';
              if (!this.fieldErrors[key]) {
                this.fieldErrors[key] = [];
              }
              if (fieldError.message) {
                this.fieldErrors[key].push(fieldError.message);
              }
            });
          }
          this.uidSuggestions = payload.uidSuggestions || [];
          return;
        }
        const parsed = parseApiError(err);
        this.errorMessage = parsed.message;
        this.fieldErrors = parsed.fieldErrors;
      }
    });
  }

  suggestUid(): void {
    const firstName = (this.form.get('firstName')?.value || '').toString();
    const lastName = (this.form.get('lastName')?.value || '').toString();
    if (!firstName && !lastName) {
      return;
    }
    this.loadingHint = true;
    this.authApi.requestUidHint(firstName, lastName).subscribe({
      next: (response: UidHintResponse) => {
        if (response.uid) {
          this.form.patchValue({ uid: response.uid });
        }
        this.loadingHint = false;
      },
      error: () => {
        this.loadingHint = false;
      }
    });
  }

  applySuggestion(uid: string): void {
    this.form.patchValue({ uid });
  }
}
