import { Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { User } from '../models/user.model';
import { MOCK_USERS, CREDENTIALS } from './mock-data.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly apiBaseUrl = 'http://localhost:8080/api';
  private _currentUser = signal<User | null>(null);

  get currentUser() { return this._currentUser(); }
  readonly currentUser$ = this._currentUser;

  constructor(private router: Router) {
    const stored = localStorage.getItem('hd_user');
    if (stored) {
      try { this._currentUser.set(JSON.parse(stored)); } catch { localStorage.removeItem('hd_user'); }
    }
  }

  async login(email: string, password: string): Promise<boolean> {
    const backendUser = await this.loginWithBackend(email, password);
    if (backendUser) {
      this.setCurrentUser(backendUser.user, backendUser.token);
      return true;
    }

    if (CREDENTIALS[email] !== password) return false;
    const user = MOCK_USERS.find(u => u.email === email);
    if (!user) return false;
    this.setCurrentUser(user, 'mock-token');
    return true;
  }

  logout() {
    this._currentUser.set(null);
    localStorage.removeItem('hd_user');
    localStorage.removeItem('hd_token');
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

  private async loginWithBackend(email: string, password: string): Promise<{ user: User; token: string } | null> {
    try {
      const response = await fetch(`${this.apiBaseUrl}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password }),
      });

      if (!response.ok) {
        return null;
      }

      const payload = await response.json();
      return {
        user: payload.user as User,
        token: payload.token as string,
      };
    } catch {
      return null;
    }
  }

  private setCurrentUser(user: User, token: string) {
    this._currentUser.set(user);
    localStorage.setItem('hd_user', JSON.stringify(user));
    localStorage.setItem('hd_token', token);
  }
}
