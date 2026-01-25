import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProfileEditApiService } from '../../services/profile-edit-api.service';
import { parseApiError } from '../../utils/api-error';

@Component({
  selector: 'app-edit-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './edit-password.component.html'
})
export class EditPasswordComponent {
  form = this.fb.group({
    currentPassword: ['', Validators.required],
    newPassword: ['', [Validators.required, Validators.minLength(6)]],
    confirmPassword: ['', [Validators.required, Validators.minLength(6)]]
  });

  errorMessage = '';
  successMessage = '';

  constructor(private fb: FormBuilder, private editApi: ProfileEditApiService) {}

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.errorMessage = '';
    this.successMessage = '';
    this.editApi.updatePassword(this.form.getRawValue() as {
      currentPassword: string;
      newPassword: string;
      confirmPassword: string;
    }).subscribe({
      next: () => {
        this.successMessage = 'Password updated.';
        this.form.reset();
      },
      error: (err) => {
        const parsed = parseApiError(err);
        this.errorMessage = parsed.message;
      }
    });
  }
}
