import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { ProfileApiService, ProfileSummary } from '../../services/profile-api.service';
import { ProfileCardComponent } from '../../shared/profile-card/profile-card.component';

@Component({
  selector: 'app-welcome',
  standalone: true,
  imports: [CommonModule, ProfileCardComponent],
  templateUrl: './welcome.component.html'
})
export class WelcomeComponent implements OnInit, OnDestroy {
  profiles: ProfileSummary[] = [];
  loading = false;
  hasNext = false;
  page = 0;
  query = '';

  private destroyed = new Subject<void>();

  constructor(
    private api: ProfileApiService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.route.queryParamMap.pipe(takeUntil(this.destroyed)).subscribe((params) => {
      this.query = (params.get('query') || '').trim();
      const rawPage = Number.parseInt(params.get('page') || '0', 10);
      this.page = Number.isFinite(rawPage) && rawPage >= 0 ? rawPage : 0;
      this.loadPage(this.page, false);
    });
  }

  ngOnDestroy(): void {
    this.destroyed.next();
    this.destroyed.complete();
  }

  loadMore(): void {
    if (this.loading || !this.hasNext) return;
    this.loadPage(this.page + 1, true);
  }

  private loadPage(page: number, append: boolean): void {
    this.loading = true;
    const request = this.query
      ? this.api.searchProfiles(this.query, page)
      : this.api.getProfiles(page);

    request.pipe(takeUntil(this.destroyed)).subscribe({
      next: (data) => {
        const items = Array.isArray(data.items) ? data.items : [];
        this.page = Number.isFinite(data.page) ? data.page : page;
        this.hasNext = Boolean(data.hasNext);
        if (append) {
          this.profiles = this.profiles.concat(items);
        } else {
          this.profiles = items;
        }
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        if (append) {
          window.alert('Request failed.');
          return;
        }
        this.fallbackToServer();
      }
    });
  }

  private fallbackToServer(): void {
    if (this.query) {
      const url = new URL('/search', window.location.origin);
      url.searchParams.set('q', this.query);
      window.location.href = url.toString();
      return;
    }
    window.location.href = '/welcome';
  }
}
