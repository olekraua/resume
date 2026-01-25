import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AccountApiService, ChangeLoginRequest } from '../../services/account-api.service';
import { SessionService } from '../../services/session.service';
import { ApiErrorResponse, parseApiError } from '../../utils/api-error';

interface UidConflictResponse {
  error: ApiErrorResponse;
  uidSuggestions?: string[];
}

@Component({
  selector: 'app-account-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './account-login.component.html'
})
export class AccountLoginComponent implements OnInit {
  form = this.fb.group({
    newUid: ['', [Validators.required, Validators.minLength(3)]]
  });

  currentUid = '';
  errorMessage = '';
  uidSuggestions: string[] = [];

  constructor(
    private fb: FormBuilder,
    private accountApi: AccountApiService,
    private sessionService: SessionService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.sessionService.load().subscribe((session) => {
      this.currentUid = session.uid || '';
      if (session.uid) {
        this.form.patchValue({ newUid: session.uid });
      }
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.errorMessage = '';
    this.uidSuggestions = [];
    const payload = this.form.getRawValue() as ChangeLoginRequest;
    this.accountApi.changeLogin(payload).subscribe({
      next: () => {
        this.sessionService.clearSession();
        this.router.navigate(['/login'], { queryParams: { loginChanged: 1 } });
      },
      error: (err) => {
        const payload = err?.error as UidConflictResponse | undefined;
        if (payload && payload.error) {
          const apiError = payload.error;
          this.errorMessage = apiError.message || 'Request failed';
          this.uidSuggestions = payload.uidSuggestions || [];
          return;
        }
        const parsed = parseApiError(err);
        this.errorMessage = parsed.message;
      }
    });
  }

  applySuggestion(uid: string): void {
    this.form.patchValue({ newUid: uid });
  }
}
