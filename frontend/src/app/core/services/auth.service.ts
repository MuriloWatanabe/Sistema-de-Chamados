import { Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { User } from '../models/user.model';
import { MOCK_USERS, CREDENTIALS } from './mock-data.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private _currentUser = signal<User | null>(null);

  get currentUser() { return this._currentUser(); }
  readonly currentUser$ = this._currentUser;

  constructor(private router: Router) {
    const stored = localStorage.getItem('hd_user');
    if (stored) {
      try { this._currentUser.set(JSON.parse(stored)); } catch { localStorage.removeItem('hd_user'); }
    }
  }

  login(email: string, password: string): boolean {
    if (CREDENTIALS[email] !== password) return false;
    const user = MOCK_USERS.find(u => u.email === email);
    if (!user) return false;
    this._currentUser.set(user);
    localStorage.setItem('hd_user', JSON.stringify(user));
    return true;
  }

  logout() {
    this._currentUser.set(null);
    localStorage.removeItem('hd_user');
    this.router.navigate(['/login']);
  }

  isLoggedIn(): boolean { return this._currentUser() !== null; }

  hasRole(...roles: number[]): boolean {
    const user = this._currentUser();
    return user ? roles.includes(user.role) : false;
  }

  isAdmin(): boolean { return this.hasRole(0); }
  isSupervisorOrAbove(): boolean { return this.hasRole(0, 1); }
  isTechnicianOrAbove(): boolean { return this.hasRole(0, 1, 2); }
}
