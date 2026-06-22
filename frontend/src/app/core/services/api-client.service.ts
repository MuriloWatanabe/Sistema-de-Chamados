import { Injectable } from '@angular/core';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class ApiClientService {
  private readonly apiBaseUrl = 'http://localhost:8080/api';

  constructor(private auth: AuthService) {}

  get<T>(path: string): Promise<T> {
    return this.request<T>('GET', path);
  }

  post<T>(path: string, body: unknown): Promise<T> {
    return this.request<T>('POST', path, body);
  }

  put<T>(path: string, body: unknown): Promise<T> {
    return this.request<T>('PUT', path, body);
  }

  patch<T>(path: string, body: unknown): Promise<T> {
    return this.request<T>('PATCH', path, body);
  }

  delete<T>(path: string): Promise<T> {
    return this.request<T>('DELETE', path);
  }

  private async request<T>(method: string, path: string, body?: unknown): Promise<T> {
    const headers: Record<string, string> = {
      Accept: 'application/json',
      ...this.auth.authHeaders(),
    };

    const hasBody = body !== undefined;
    if (hasBody) {
      headers['Content-Type'] = 'application/json';
    }

    const response = await fetch(`${this.apiBaseUrl}${path}`, {
      method,
      headers,
      body: hasBody ? JSON.stringify(body) : undefined,
    });

    const text = await response.text();
    if (!response.ok) {
      throw new Error(this.extractErrorMessage(text, response.status));
    }

    if (!text.trim()) {
      return undefined as T;
    }

    return JSON.parse(text) as T;
  }

  private extractErrorMessage(payload: string, status: number): string {
    if (!payload.trim()) {
      return status === 401 ? 'Sessao expirada.' : `Requisicao falhou (${status}).`;
    }

    try {
      const parsed = JSON.parse(payload) as { message?: string; error?: string };
      if (parsed.message?.trim()) {
        return parsed.message;
      }
      if (parsed.error?.trim()) {
        return parsed.error;
      }
    } catch {
      // fall through to raw payload
    }

    return payload.trim();
  }
}
