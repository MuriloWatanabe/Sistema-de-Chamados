import { Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { User } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly apiBaseUrl = 'http://localhost:8080/api';
  private _currentUser = signal<User | null>(null);
  private expiryTimer: ReturnType<typeof setTimeout> | null = null;

  get currentUser() { return this._currentUser(); }
  readonly currentUser$ = this._currentUser;

  constructor(private router: Router) {
    const storedUser = localStorage.getItem('hd_user');
    const storedToken = localStorage.getItem('hd_token');

    if (storedUser && storedToken && !this.isTokenExpired(storedToken)) {
      try {
        this._currentUser.set(JSON.parse(storedUser));
        this.scheduleExpiry(storedToken);
        void this.validateStoredSession();
      } catch {
        this.clearSession();
      }
    } else {
      this.clearSession();
    }
  }

  async login(email: string, password: string): Promise<boolean> {
    const backendUser = await this.loginWithBackend(email, password);
    if (backendUser) {
      this.setCurrentUser(backendUser.user, backendUser.token);
      return true;
    }
    return false;
  }

  logout() {
    const token = this.token;
    if (token) {
      // Revoga o token no servidor; a sessao local e encerrada mesmo se a API nao responder.
      fetch(`${this.apiBaseUrl}/auth/logout`, {
        method: 'POST',
        headers: { Authorization: `Bearer ${token}` },
      }).catch(() => undefined);
    }
    this.clearSession();
    this.router.navigate(['/login']);
  }

  /** Chamado quando a API responde 401: token expirado, revogado ou invalido. */
  handleUnauthorized() {
    this.clearSession();
    if (this.router.url !== '/login') {
      this.router.navigate(['/login']);
    }
  }

  isLoggedIn(): boolean {
    const token = this.token;
    return this._currentUser() !== null && !!token && !this.isTokenExpired(token);
  }

  get token(): string | null {
    return localStorage.getItem('hd_token');
  }

  authHeaders(): Record<string, string> {
    const token = this.token;
    return token ? { Authorization: `Bearer ${token}` } : {};
  }

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
      if (!payload?.user || !payload?.token) {
        return null;
      }
      return {
        user: payload.user as User,
        token: payload.token as string,
      };
    } catch {
      return null;
    }
  }

  private async validateStoredSession() {
    const token = this.token;
    if (!token) {
      this.clearSession();
      return;
    }

    try {
      const response = await fetch(`${this.apiBaseUrl}/auth/me`, {
        headers: { Authorization: `Bearer ${token}` },
      });

      if (!response.ok) {
        throw new Error('Invalid session');
      }

      const user = (await response.json()) as User;
      this._currentUser.set(user);
      localStorage.setItem('hd_user', JSON.stringify(user));
    } catch {
      this.clearSession();
      if (this.router.url !== '/login') {
        this.router.navigate(['/login']);
      }
    }
  }

  private setCurrentUser(user: User, token: string) {
    this._currentUser.set(user);
    localStorage.setItem('hd_user', JSON.stringify(user));
    localStorage.setItem('hd_token', token);
    this.scheduleExpiry(token);
  }

  /** Le o "exp" (segundos) do payload do JWT. A assinatura so e validada no backend. */
  private tokenExpiresAt(token: string): number | null {
    try {
      const payload = token.split('.')[1];
      if (!payload) return null;
      const json = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
      const exp = JSON.parse(json)?.exp;
      return typeof exp === 'number' ? exp * 1000 : null;
    } catch {
      return null;
    }
  }

  private isTokenExpired(token: string): boolean {
    const expiresAt = this.tokenExpiresAt(token);
    return expiresAt !== null && expiresAt <= Date.now();
  }

  private scheduleExpiry(token: string) {
    this.cancelExpiry();
    const expiresAt = this.tokenExpiresAt(token);
    if (expiresAt === null) return;
    // setTimeout aceita no maximo ~24,8 dias; tokens de longa duracao ficam so com a checagem do backend.
    const delay = expiresAt - Date.now();
    if (delay > 0 && delay < 2_147_483_647) {
      this.expiryTimer = setTimeout(() => this.handleUnauthorized(), delay);
    }
  }

  private cancelExpiry() {
    if (this.expiryTimer !== null) {
      clearTimeout(this.expiryTimer);
      this.expiryTimer = null;
    }
  }

  private clearSession() {
    this.cancelExpiry();
    this._currentUser.set(null);
    localStorage.removeItem('hd_user');
    localStorage.removeItem('hd_token');
  }
}
