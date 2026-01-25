import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AccountApiService } from '../../services/account-api.service';
import { SessionService } from '../../services/session.service';
import { parseApiError } from '../../utils/api-error';

@Component({
  selector: 'app-account-remove',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './account-remove.component.html'
})
export class AccountRemoveComponent {
  confirmed = false;
  errorMessage = '';

  constructor(
    private accountApi: AccountApiService,
    private sessionService: SessionService,
    private router: Router
  ) {}

  remove(): void {
    if (!this.confirmed) {
      this.errorMessage = 'Please confirm account removal.';
      return;
    }
    this.errorMessage = '';
    this.accountApi.removeAccount().subscribe({
      next: () => {
        this.sessionService.clearSession();
        this.router.navigate(['/welcome'], { queryParams: { removed: 1 } });
      },
      error: (err) => {
        const parsed = parseApiError(err);
        this.errorMessage = parsed.message;
      }
    });
  }
}
