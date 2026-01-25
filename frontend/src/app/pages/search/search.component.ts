import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { ProfileApiService, ProfileSummary } from '../../services/profile-api.service';
import { ProfileCardComponent } from '../../shared/profile-card/profile-card.component';

@Component({
  selector: 'app-search',
  standalone: true,
  imports: [CommonModule, ProfileCardComponent],
  templateUrl: './search.component.html'
})
export class SearchComponent implements OnInit, OnDestroy {
  profiles: ProfileSummary[] = [];
  query = '';
  loading = false;

  private destroyed = new Subject<void>();

  constructor(
    private api: ProfileApiService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.route.queryParamMap.pipe(takeUntil(this.destroyed)).subscribe((params) => {
      const q = (params.get('q') || '').trim();
      if (!q) {
        this.router.navigate(['/welcome']);
        return;
      }
      this.query = q;
      const rawPage = Number.parseInt(params.get('page') || '0', 10);
      const page = Number.isFinite(rawPage) && rawPage >= 0 ? rawPage : 0;
      this.loadPage(page);
    });
  }

  ngOnDestroy(): void {
    this.destroyed.next();
    this.destroyed.complete();
  }

  private loadPage(page: number): void {
    this.loading = true;
    this.api.searchProfiles(this.query, page).pipe(takeUntil(this.destroyed)).subscribe({
      next: (data) => {
        this.profiles = Array.isArray(data.items) ? data.items : [];
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        const url = new URL('/search', window.location.origin);
        url.searchParams.set('q', this.query);
        window.location.href = url.toString();
      }
    });
  }
}
