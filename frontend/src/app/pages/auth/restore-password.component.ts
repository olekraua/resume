import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthApiService, RestorePasswordRequest } from '../../services/auth-api.service';
import { parseApiError } from '../../utils/api-error';

@Component({
  selector: 'app-restore-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './restore-password.component.html'
})
export class RestorePasswordComponent implements OnInit {
  form = this.fb.group({
    password: ['', [Validators.required, Validators.minLength(6)]],
    confirmPassword: ['', [Validators.required, Validators.minLength(6)]]
  });

  errorMessage = '';
  invalidToken = false;
  token = '';

  constructor(
    private fb: FormBuilder,
    private authApi: AuthApiService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.token = this.route.snapshot.paramMap.get('token') || '';
    if (!this.token) {
      this.invalidToken = true;
      return;
    }
    this.authApi.checkRestoreToken(this.token).subscribe({
      next: () => {
        this.invalidToken = false;
      },
      error: () => {
        this.invalidToken = true;
      }
    });
  }

  submit(): void {
    if (this.form.invalid || this.invalidToken) {
      this.form.markAllAsTouched();
      return;
    }
    this.errorMessage = '';
    const payload = this.form.getRawValue() as RestorePasswordRequest;
    this.authApi.resetPassword(this.token, payload).subscribe({
      next: () => {
        this.router.navigate(['/login'], { queryParams: { restored: 1 } });
      },
      error: (err) => {
        const parsed = parseApiError(err);
        this.errorMessage = parsed.message;
      }
    });
  }
}
