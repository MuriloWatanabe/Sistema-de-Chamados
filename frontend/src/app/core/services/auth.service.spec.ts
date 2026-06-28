import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';
import { User } from '../models/user.model';

const mockUser: User = {
  id: 1,
  name: 'João Silva',
  email: 'joao@helpdesk.com',
  active: true,
  role: 2,
  createdAt: '2024-01-01T00:00:00',
  updatedAt: '2024-01-01T00:00:00',
};

describe('AuthService', () => {
  let service: AuthService;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(() => {
    localStorage.clear();

    routerSpy = jasmine.createSpyObj('Router', ['navigate', 'createUrlTree']);
    Object.defineProperty(routerSpy, 'url', { get: () => '/dashboard' });

    TestBed.configureTestingModule({
      providers: [
        AuthService,
        { provide: Router, useValue: routerSpy },
      ],
    });

    service = TestBed.inject(AuthService);
  });

  afterEach(() => {
    localStorage.clear();
  });

  // --- isLoggedIn ---

  it('deve retornar false quando nao ha usuario logado', () => {
    expect(service.isLoggedIn()).toBeFalse();
  });

  it('deve retornar false quando ha usuario mas nao ha token', () => {
    localStorage.setItem('hd_user', JSON.stringify(mockUser));
    expect(service.isLoggedIn()).toBeFalse();
  });

  // --- hasRole ---

  it('deve retornar false quando nao ha usuario logado', () => {
    expect(service.hasRole(2)).toBeFalse();
  });

  // --- isAdmin / isSupervisorOrAbove / isTechnicianOrAbove ---

  it('isAdmin deve retornar false quando nao ha usuario', () => {
    expect(service.isAdmin()).toBeFalse();
  });

  it('isSupervisorOrAbove deve retornar false quando nao ha usuario', () => {
    expect(service.isSupervisorOrAbove()).toBeFalse();
  });

  it('isTechnicianOrAbove deve retornar false quando nao ha usuario', () => {
    expect(service.isTechnicianOrAbove()).toBeFalse();
  });

  // --- authHeaders ---

  it('deve retornar objeto vazio quando nao ha token', () => {
    const headers = service.authHeaders();
    expect(headers).toEqual({});
  });

  it('deve retornar cabecalho Authorization quando ha token', () => {
    localStorage.setItem('hd_token', 'abc123');
    const headers = service.authHeaders();
    expect(headers['Authorization']).toBe('Bearer abc123');
  });

  // --- token getter ---

  it('deve retornar null quando nao ha token no localStorage', () => {
    expect(service.token).toBeNull();
  });

  it('deve retornar o token armazenado no localStorage', () => {
    localStorage.setItem('hd_token', 'meu-token');
    expect(service.token).toBe('meu-token');
  });

  // --- logout ---

  it('deve navegar para /login ao fazer logout', () => {
    service.logout();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('deve limpar o localStorage ao fazer logout', () => {
    localStorage.setItem('hd_token', 'abc');
    localStorage.setItem('hd_user', JSON.stringify(mockUser));

    service.logout();

    expect(localStorage.getItem('hd_token')).toBeNull();
    expect(localStorage.getItem('hd_user')).toBeNull();
  });

  it('deve limpar o usuario atual ao fazer logout', () => {
    service.logout();
    expect(service.currentUser).toBeNull();
  });

  // --- login (with fetch mock) ---

  it('deve retornar false quando o backend retorna erro', async () => {
    spyOn(globalThis, 'fetch').and.returnValue(
      Promise.resolve(new Response('', { status: 401 }))
    );

    const result = await service.login('wrong@email.com', 'badpassword');

    expect(result).toBeFalse();
  });

  it('deve retornar false quando fetch lanca excecao', async () => {
    spyOn(globalThis, 'fetch').and.returnValue(Promise.reject(new Error('Network error')));

    const result = await service.login('user@email.com', 'pass');

    expect(result).toBeFalse();
  });

  it('deve retornar true e salvar usuario quando login e bem-sucedido', async () => {
    const payload = { user: mockUser, token: 'jwt-token' };
    spyOn(globalThis, 'fetch').and.returnValue(
      Promise.resolve(
        new Response(JSON.stringify(payload), {
          status: 200,
          headers: { 'Content-Type': 'application/json' },
        })
      )
    );

    const result = await service.login('joao@helpdesk.com', 'senha123');

    expect(result).toBeTrue();
    expect(localStorage.getItem('hd_token')).toBe('jwt-token');
    expect(service.currentUser?.email).toBe('joao@helpdesk.com');
  });

  it('deve retornar false quando resposta nao contem user ou token', async () => {
    spyOn(globalThis, 'fetch').and.returnValue(
      Promise.resolve(
        new Response(JSON.stringify({ data: 'incompleto' }), {
          status: 200,
          headers: { 'Content-Type': 'application/json' },
        })
      )
    );

    const result = await service.login('user@email.com', 'pass');

    expect(result).toBeFalse();
  });
});
