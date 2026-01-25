import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

@Component({
  selector: 'app-restore-success',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './restore-success.component.html'
})
export class RestoreSuccessComponent {
  restoreLink: string | null = null;

  constructor(private router: Router) {
    const nav = this.router.getCurrentNavigation();
    this.restoreLink = (nav?.extras?.state as { restoreLink?: string } | undefined)?.restoreLink || null;
  }
}
