import { TestBed } from '@angular/core/testing';
import { Router, UrlTree } from '@angular/router';
import { adminGuard } from './admin.guard';
import { AuthService } from '../services/auth.service';

describe('adminGuard', () => {
  let authSpy: jasmine.SpyObj<AuthService>;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(() => {
    authSpy = jasmine.createSpyObj('AuthService', ['isAdmin']);
    routerSpy = jasmine.createSpyObj('Router', ['createUrlTree', 'navigate']);

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: authSpy },
        { provide: Router, useValue: routerSpy },
      ],
    });
  });

  it('deve permitir acesso quando o usuario e administrador', () => {
    authSpy.isAdmin.and.returnValue(true);

    const result = TestBed.runInInjectionContext(() => adminGuard({} as any, {} as any));

    expect(result).toBeTrue();
  });

  it('deve redirecionar para /dashboard quando o usuario nao e administrador', () => {
    authSpy.isAdmin.and.returnValue(false);
    const urlTree = new UrlTree();
    routerSpy.createUrlTree.and.returnValue(urlTree);

    const result = TestBed.runInInjectionContext(() => adminGuard({} as any, {} as any));

    expect(routerSpy.createUrlTree).toHaveBeenCalledWith(['/dashboard']);
    expect(result).toBe(urlTree);
  });

  it('deve chamar isAdmin do AuthService', () => {
    authSpy.isAdmin.and.returnValue(false);
    routerSpy.createUrlTree.and.returnValue(new UrlTree());

    TestBed.runInInjectionContext(() => adminGuard({} as any, {} as any));

    expect(authSpy.isAdmin).toHaveBeenCalled();
  });
});
