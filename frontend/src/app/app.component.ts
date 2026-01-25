import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NavigationEnd, Router, RouterLink, RouterOutlet } from '@angular/router';
import { filter, Subject, takeUntil } from 'rxjs';
import { AuthApiService } from './services/auth-api.service';
import { SessionInfo, SessionService } from './services/session.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, RouterOutlet],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit, OnDestroy {
  searchQuery = '';
  session: SessionInfo | null = null;

  private destroyed = new Subject<void>();

  constructor(
    private router: Router,
    private sessionService: SessionService,
    private authApi: AuthApiService
  ) {}

  ngOnInit(): void {
    this.sessionService.load().pipe(takeUntil(this.destroyed)).subscribe((session) => {
      this.session = session;
    });
    this.syncSearchQuery();

    this.router.events.pipe(
      filter((event): event is NavigationEnd => event instanceof NavigationEnd),
      takeUntil(this.destroyed)
    ).subscribe(() => {
      this.syncSearchQuery();
      window.scrollTo(0, 0);
    });
  }

  ngOnDestroy(): void {
    this.destroyed.next();
    this.destroyed.complete();
  }

  onSearch(): void {
    const query = (this.searchQuery || '').trim();
    if (!query) {
      this.router.navigate(['/welcome']);
      return;
    }
    this.router.navigate(['/search'], { queryParams: { q: query } });
  }

  onLogout(event: Event): void {
    event.preventDefault();
    this.authApi.logout().subscribe({
      next: () => {
        this.sessionService.clearSession();
        this.router.navigate(['/welcome'], { queryParams: { logout: 1 } });
      },
      error: () => {
        this.sessionService.clearSession();
        this.router.navigate(['/welcome']);
      }
    });
  }

  private syncSearchQuery(): void {
    const tree = this.router.parseUrl(this.router.url);
    const query = (tree.queryParams['q'] || tree.queryParams['query'] || '').trim();
    this.searchQuery = query;
  }
}
