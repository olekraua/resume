import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthApiService, LoginRequest } from '../../services/auth-api.service';
import { SessionService } from '../../services/session.service';
import { parseApiError } from '../../utils/api-error';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html'
})
export class LoginComponent implements OnInit {
  form = this.fb.group({
    username: ['', Validators.required],
    password: ['', Validators.required],
    rememberMe: [false]
  });

  errorMessage = '';
  infoMessage = '';
  infoClass = 'c-alert c-alert--info';
  fieldErrors: Record<string, string[]> = {};
  showPassword = false;

  constructor(
    private fb: FormBuilder,
    private authApi: AuthApiService,
    private sessionService: SessionService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.route.queryParamMap.subscribe((params) => {
      this.infoMessage = '';
      this.infoClass = 'c-alert c-alert--info';
      if (params.has('expired')) {
        this.infoMessage = 'Session expired. Please sign in again.';
        this.infoClass = 'c-alert c-alert--warning';
      } else if (params.has('restored')) {
        this.infoMessage = 'Password reset successfully. Please sign in.';
        this.infoClass = 'c-alert c-alert--success';
      } else if (params.has('logout')) {
        this.infoMessage = 'Signed out successfully.';
        this.infoClass = 'c-alert c-alert--success';
      } else if (params.has('loginChanged')) {
        this.infoMessage = 'Login changed. Please sign in with your new username.';
        this.infoClass = 'c-alert c-alert--warning';
      }
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.errorMessage = '';
    this.fieldErrors = {};
    const payload = this.form.getRawValue() as LoginRequest;
    this.authApi.login(payload).subscribe({
      next: (session) => {
        this.sessionService.setSession(session);
        const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl');
        if (returnUrl) {
          this.router.navigateByUrl(returnUrl);
          return;
        }
        if (session.uid) {
          this.router.navigate(['/', session.uid]);
          return;
        }
        this.router.navigate(['/welcome']);
      },
      error: (err) => {
        const parsed = parseApiError(err);
        this.errorMessage = parsed.message;
        this.fieldErrors = parsed.fieldErrors;
      }
    });
  }
}
