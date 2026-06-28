import { TestBed } from '@angular/core/testing';
import { Router, UrlTree } from '@angular/router';
import { authGuard } from './auth.guard';
import { AuthService } from '../services/auth.service';

describe('authGuard', () => {
  let authSpy: jasmine.SpyObj<AuthService>;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(() => {
    authSpy = jasmine.createSpyObj('AuthService', ['isLoggedIn']);
    routerSpy = jasmine.createSpyObj('Router', ['createUrlTree', 'navigate']);

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: authSpy },
        { provide: Router, useValue: routerSpy },
      ],
    });
  });

  it('deve permitir acesso quando o usuario esta logado', () => {
    authSpy.isLoggedIn.and.returnValue(true);

    const result = TestBed.runInInjectionContext(() => authGuard({} as any, {} as any));

    expect(result).toBeTrue();
  });

  it('deve redirecionar para /login quando o usuario nao esta logado', () => {
    authSpy.isLoggedIn.and.returnValue(false);
    const urlTree = new UrlTree();
    routerSpy.createUrlTree.and.returnValue(urlTree);

    const result = TestBed.runInInjectionContext(() => authGuard({} as any, {} as any));

    expect(routerSpy.createUrlTree).toHaveBeenCalledWith(['/login']);
    expect(result).toBe(urlTree);
  });

  it('deve chamar isLoggedIn do AuthService', () => {
    authSpy.isLoggedIn.and.returnValue(true);

    TestBed.runInInjectionContext(() => authGuard({} as any, {} as any));

    expect(authSpy.isLoggedIn).toHaveBeenCalled();
  });
});
