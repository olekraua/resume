import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthApiService, RestoreRequest } from '../../services/auth-api.service';
import { parseApiError } from '../../utils/api-error';

@Component({
  selector: 'app-restore',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './restore.component.html'
})
export class RestoreComponent implements OnInit {
  form = this.fb.group({
    identifier: ['', Validators.required]
  });

  errorMessage = '';
  infoMessage = '';

  constructor(
    private fb: FormBuilder,
    private authApi: AuthApiService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.route.queryParamMap.subscribe((params) => {
      this.infoMessage = params.has('invalid') ? 'Invalid or expired restore link.' : '';
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.errorMessage = '';
    const payload = this.form.getRawValue() as RestoreRequest;
    this.authApi.requestRestore(payload).subscribe({
      next: (response) => {
        this.router.navigate(['/restore/success'], { state: { restoreLink: response.restoreLink || null } });
      },
      error: (err) => {
        const parsed = parseApiError(err);
        this.errorMessage = parsed.message;
      }
    });
  }
}
