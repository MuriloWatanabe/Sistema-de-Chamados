import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { LoginComponent } from './login.component';
import { AuthService } from '../../../core/services/auth.service';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let authSpy: jasmine.SpyObj<AuthService>;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    authSpy = jasmine.createSpyObj('AuthService', ['login']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [
        { provide: AuthService, useValue: authSpy },
        { provide: Router, useValue: routerSpy },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
  });

  // --- fillCredentials ---

  it('fillCredentials deve preencher email e password', () => {
    component.fillCredentials('admin@helpdesk.com', 'admin123');

    expect(component.email).toBe('admin@helpdesk.com');
    expect(component.password).toBe('admin123');
  });

  it('fillCredentials deve sobrescrever credenciais existentes', () => {
    component.email = 'outro@email.com';
    component.password = 'outrasenha';

    component.fillCredentials('novo@email.com', 'novasenha');

    expect(component.email).toBe('novo@email.com');
    expect(component.password).toBe('novasenha');
  });

  // --- hints ---

  it('deve ter 4 hints de credenciais', () => {
    expect(component.hints.length).toBe(4);
  });

  it('deve conter hint de Administrador', () => {
    const admin = component.hints.find(h => h.role === 'Administrador');
    expect(admin).toBeTruthy();
    expect(admin?.email).toBeTruthy();
  });

  // --- sinais iniciais ---

  it('error deve ser vazio no inicio', () => {
    expect(component.error()).toBe('');
  });

  it('loading deve ser false no inicio', () => {
    expect(component.loading()).toBeFalse();
  });

  // --- onSubmit (login bem-sucedido) ---

  it('deve navegar para /dashboard quando o login e bem-sucedido', async () => {
    authSpy.login.and.returnValue(Promise.resolve(true));
    component.email = 'user@helpdesk.com';
    component.password = 'senha123';

    await component.onSubmit();

    expect(routerSpy.navigate).toHaveBeenCalledWith(['/dashboard']);
  });

  it('deve limpar o erro antes de submeter', async () => {
    authSpy.login.and.returnValue(Promise.resolve(true));
    component.error.set('Erro antigo');

    await component.onSubmit();

    expect(component.error()).toBe('');
  });

  it('deve chamar auth.login com email e password', async () => {
    authSpy.login.and.returnValue(Promise.resolve(true));
    component.email = 'joao@helpdesk.com';
    component.password = 'minha-senha';

    await component.onSubmit();

    expect(authSpy.login).toHaveBeenCalledWith('joao@helpdesk.com', 'minha-senha');
  });

  // --- onSubmit (login falhou) ---

  it('deve exibir mensagem de erro quando o login falha', async () => {
    authSpy.login.and.returnValue(Promise.resolve(false));

    await component.onSubmit();

    expect(component.error()).toBe('E-mail ou senha incorretos.');
  });

  it('nao deve navegar quando o login falha', async () => {
    authSpy.login.and.returnValue(Promise.resolve(false));

    await component.onSubmit();

    expect(routerSpy.navigate).not.toHaveBeenCalled();
  });

  it('loading deve ser false apos o submit completar', async () => {
    authSpy.login.and.returnValue(Promise.resolve(true));

    await component.onSubmit();

    expect(component.loading()).toBeFalse();
  });
});
