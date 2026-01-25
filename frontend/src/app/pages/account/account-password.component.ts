import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AccountApiService, ChangePasswordRequest } from '../../services/account-api.service';
import { parseApiError } from '../../utils/api-error';

@Component({
  selector: 'app-account-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './account-password.component.html'
})
export class AccountPasswordComponent {
  form = this.fb.group({
    currentPassword: ['', Validators.required],
    newPassword: ['', [Validators.required, Validators.minLength(6)]],
    confirmPassword: ['', [Validators.required, Validators.minLength(6)]]
  });

  errorMessage = '';
  successMessage = '';

  constructor(private fb: FormBuilder, private accountApi: AccountApiService) {}

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.errorMessage = '';
    this.successMessage = '';
    const payload = this.form.getRawValue() as ChangePasswordRequest;
    this.accountApi.changePassword(payload).subscribe({
      next: () => {
        this.successMessage = 'Password updated successfully.';
        this.form.reset();
      },
      error: (err) => {
        const parsed = parseApiError(err);
        this.errorMessage = parsed.message;
      }
    });
  }
}
