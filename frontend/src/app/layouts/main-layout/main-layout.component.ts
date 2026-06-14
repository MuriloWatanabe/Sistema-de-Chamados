import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { ROLES } from '../../core/models/user.model';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './main-layout.component.html',
  styleUrl: './main-layout.component.css',
})
export class MainLayoutComponent {
  auth = inject(AuthService);
  roles = ROLES;

  get user() { return this.auth.currentUser; }
  get userRole() { return this.user ? ROLES[this.user.role] : null; }
  get initials() {
    if (!this.user) return '';
    return this.user.name.split(' ').slice(0, 2).map(n => n[0]).join('').toUpperCase();
  }

  logout() { this.auth.logout(); }
}
