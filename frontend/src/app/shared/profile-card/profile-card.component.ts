import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ProfileSummary } from '../../services/profile-api.service';

@Component({
  selector: 'app-profile-card',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './profile-card.component.html'
})
export class ProfileCardComponent {
  @Input({ required: true }) profile!: ProfileSummary;

  get fullName(): string {
    return (this.profile.fullName || '').trim();
  }

  get title(): string {
    const name = this.fullName;
    const age = Number.isFinite(this.profile.age) ? this.profile.age : null;
    if (name && age != null) {
      return `${name}, ${age}`;
    }
    return name;
  }

  get location(): string {
    const parts: string[] = [];
    if (this.profile.city) parts.push(this.profile.city);
    if (this.profile.country) parts.push(this.profile.country);
    return parts.join(', ');
  }

  get photo(): string {
    return this.profile.smallPhoto || '/img/profile-placeholder.png';
  }
}
