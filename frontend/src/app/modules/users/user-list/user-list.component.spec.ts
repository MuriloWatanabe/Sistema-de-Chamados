import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { UserListComponent } from './user-list.component';
import { AuthService } from '../../../core/services/auth.service';
import { HelpdeskApiService } from '../../../core/services/helpdesk-api.service';
import { User } from '../../../core/models/user.model';

const makeUser = (id: number, name: string, role = 3): User => ({
  id, name, email: `${name.toLowerCase()}@test.com`,
  active: true, role,
  createdAt: '', updatedAt: '',
});

describe('UserListComponent', () => {
  let component: UserListComponent;
  let apiSpy: jasmine.SpyObj<HelpdeskApiService>;
  let authSpy: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    apiSpy = jasmine.createSpyObj('HelpdeskApiService', ['listUsers', 'createUser', 'updateUser', 'deleteUser']);
    authSpy = jasmine.createSpyObj('AuthService', ['isAdmin', 'isTechnicianOrAbove']);

    await TestBed.configureTestingModule({
      imports: [UserListComponent],
      providers: [
        { provide: HelpdeskApiService, useValue: apiSpy },
        { provide: AuthService, useValue: authSpy },
        { provide: Router, useValue: jasmine.createSpyObj('Router', ['navigate']) },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(UserListComponent);
    component = fixture.componentInstance;
  });

  // --- getInitials ---

  it('getInitials deve retornar iniciais de nome e sobrenome', () => {
    expect(component.getInitials('João Silva')).toBe('JS');
  });

  it('getInitials deve retornar apenas primeira inicial para nome simples', () => {
    expect(component.getInitials('Admin')).toBe('A');
  });

  it('getInitials deve retornar apenas duas iniciais mesmo com tres palavras', () => {
    expect(component.getInitials('Ana Maria Santos')).toBe('AM');
  });

  it('getInitials deve retornar em maiusculas', () => {
    expect(component.getInitials('jose ferreira')).toBe('JF');
  });

  // --- openCreate ---

  it('openCreate deve abrir o modal', () => {
    component.openCreate();
    expect(component.showModal()).toBeTrue();
  });

  it('openCreate deve limpar o usuario em edicao', () => {
    component.editingUser.set(makeUser(1, 'Existente'));

    component.openCreate();

    expect(component.editingUser()).toBeNull();
  });

  it('openCreate deve resetar o formulario', () => {
    component.form = { name: 'Antigo', email: 'antigo@test.com', password: 'senha', role: 0, active: false };

    component.openCreate();

    expect(component.form.name).toBe('');
    expect(component.form.email).toBe('');
    expect(component.form.password).toBe('');
    expect(component.form.role).toBe(3);
    expect(component.form.active).toBeTrue();
  });

  it('openCreate deve limpar o erro do formulario', () => {
    component.formError = 'Erro anterior';

    component.openCreate();

    expect(component.formError).toBe('');
  });

  // --- openEdit ---

  it('openEdit deve abrir o modal com dados do usuario', () => {
    const user = makeUser(5, 'Maria');

    component.openEdit(user);

    expect(component.showModal()).toBeTrue();
    expect(component.editingUser()).toEqual(user);
  });

  it('openEdit deve preencher o formulario com dados do usuario', () => {
    const user: User = { id: 5, name: 'Maria Silva', email: 'maria@test.com', active: false, role: 2, createdAt: '', updatedAt: '' };

    component.openEdit(user);

    expect(component.form.name).toBe('Maria Silva');
    expect(component.form.email).toBe('maria@test.com');
    expect(component.form.role).toBe(2);
    expect(component.form.active).toBeFalse();
    expect(component.form.password).toBe('');
  });

  it('openEdit deve limpar o erro do formulario', () => {
    component.formError = 'Erro existente';

    component.openEdit(makeUser(1, 'User'));

    expect(component.formError).toBe('');
  });

  // --- closeModal ---

  it('closeModal deve fechar o modal', () => {
    component.showModal.set(true);

    component.closeModal();

    expect(component.showModal()).toBeFalse();
  });

  // --- saveUser: validacoes ---

  it('saveUser deve exibir erro quando nome esta vazio', async () => {
    component.form.name = '';
    component.form.email = 'test@test.com';

    await component.saveUser();

    expect(component.formError).toBe('Preencha nome e e-mail.');
    expect(apiSpy.createUser).not.toHaveBeenCalled();
  });

  it('saveUser deve exibir erro quando email esta vazio', async () => {
    component.form.name = 'Nome';
    component.form.email = '';

    await component.saveUser();

    expect(component.formError).toBe('Preencha nome e e-mail.');
  });

  it('saveUser deve exibir erro quando criando usuario com senha curta', async () => {
    component.editingUser.set(null);
    component.form = { name: 'Novo', email: 'novo@test.com', password: '123', role: 3, active: true };

    await component.saveUser();

    expect(component.formError).toBe('Informe uma senha com pelo menos 8 caracteres.');
    expect(apiSpy.createUser).not.toHaveBeenCalled();
  });

  it('saveUser deve exibir erro quando senha de atualizacao e curta', async () => {
    component.editingUser.set(makeUser(1, 'Existente'));
    component.form = { name: 'Existente', email: 'e@test.com', password: '123', role: 3, active: true };

    await component.saveUser();

    expect(component.formError).toBe('A senha deve ter pelo menos 8 caracteres.');
  });

  it('saveUser deve chamar createUser quando nao ha usuario em edicao', async () => {
    component.editingUser.set(null);
    component.form = { name: 'Novo Usuario', email: 'novo@test.com', password: 'senha1234', role: 3, active: true };
    apiSpy.createUser.and.returnValue(Promise.resolve(makeUser(10, 'Novo Usuario')));
    apiSpy.listUsers.and.returnValue(Promise.resolve([]));

    await component.saveUser();

    expect(apiSpy.createUser).toHaveBeenCalled();
  });

  it('saveUser deve chamar updateUser quando ha usuario em edicao', async () => {
    const user = makeUser(5, 'Editado');
    component.editingUser.set(user);
    component.form = { name: 'Editado', email: 'e@test.com', password: '', role: 3, active: true };
    apiSpy.updateUser.and.returnValue(Promise.resolve(user));
    apiSpy.listUsers.and.returnValue(Promise.resolve([]));

    await component.saveUser();

    expect(apiSpy.updateUser).toHaveBeenCalledWith(5, jasmine.objectContaining({ name: 'Editado' }));
  });

  it('saveUser deve fechar o modal apos salvar com sucesso', async () => {
    component.editingUser.set(null);
    component.form = { name: 'Novo', email: 'novo@test.com', password: 'senha1234', role: 3, active: true };
    apiSpy.createUser.and.returnValue(Promise.resolve(makeUser(10, 'Novo')));
    apiSpy.listUsers.and.returnValue(Promise.resolve([]));
    component.showModal.set(true);

    await component.saveUser();

    expect(component.showModal()).toBeFalse();
  });

  it('saveUser deve exibir erro quando a API falha', async () => {
    component.editingUser.set(null);
    component.form = { name: 'Novo', email: 'novo@test.com', password: 'senha1234', role: 3, active: true };
    apiSpy.createUser.and.returnValue(Promise.reject(new Error('E-mail ja cadastrado')));

    await component.saveUser();

    expect(component.formError).toBe('E-mail ja cadastrado');
  });

  // --- load ---

  it('load deve preencher a lista de usuarios', async () => {
    const users = [makeUser(1, 'Ana'), makeUser(2, 'Bob')];
    apiSpy.listUsers.and.returnValue(Promise.resolve(users));

    await component.load();

    expect(component.users.length).toBe(2);
  });

  it('load deve zerar a lista quando a API falha', async () => {
    apiSpy.listUsers.and.returnValue(Promise.reject(new Error('Falha')));

    await component.load();

    expect(component.users).toEqual([]);
  });
});
