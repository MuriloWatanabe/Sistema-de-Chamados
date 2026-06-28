import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { DashboardComponent } from './dashboard.component';
import { AuthService } from '../../core/services/auth.service';
import { HelpdeskApiService } from '../../core/services/helpdesk-api.service';
import { User } from '../../core/models/user.model';
import { DashboardStats } from '../../core/models/api-contracts';
import { Ticket } from '../../core/models/ticket.model';

const mockUser: User = {
  id: 1, name: 'João Silva', email: 'joao@helpdesk.com',
  active: true, role: 2, createdAt: '', updatedAt: '',
};

const mockStats: DashboardStats = { total: 10, open: 3, inProgress: 2, resolved: 4, closed: 1, urgent: 1 };

const makeTicket = (id: number): Ticket => ({
  id, title: `Ticket ${id}`, description: '', status: 0, priority: 1,
  requester: { id: 1, name: 'User', email: 'u@t.com' },
  createdAt: '', updatedAt: '',
});

describe('DashboardComponent', () => {
  let component: DashboardComponent;
  let apiSpy: jasmine.SpyObj<HelpdeskApiService>;
  let authSpy: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    apiSpy = jasmine.createSpyObj('HelpdeskApiService', ['getDashboardStats', 'listTickets']);
    authSpy = jasmine.createSpyObj('AuthService', ['isAdmin', 'isTechnicianOrAbove']);
    Object.defineProperty(authSpy, 'currentUser', { get: () => mockUser, configurable: true });

    await TestBed.configureTestingModule({
      imports: [DashboardComponent],
      providers: [
        { provide: HelpdeskApiService, useValue: apiSpy },
        { provide: AuthService, useValue: authSpy },
        { provide: Router, useValue: jasmine.createSpyObj('Router', ['navigate']) },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
  });

  // --- getter user ---

  it('user deve retornar o usuario atual do AuthService', () => {
    expect(component.user).toEqual(mockUser);
  });

  // --- getter firstName ---

  it('firstName deve retornar o primeiro nome do usuario', () => {
    expect(component.firstName).toBe('João');
  });

  it('firstName deve retornar string vazia quando nao ha usuario', () => {
    Object.defineProperty(authSpy, 'currentUser', { get: () => null, configurable: true });

    expect(component.firstName).toBe('');
  });

  it('firstName deve retornar apenas o primeiro nome quando ha nome composto', () => {
    Object.defineProperty(authSpy, 'currentUser', {
      get: () => ({ ...mockUser, name: 'Ana Maria Silva' }),
      configurable: true,
    });

    expect(component.firstName).toBe('Ana');
  });

  // --- getter greeting ---

  it('greeting deve retornar "Bom dia" para hora menor que 12', () => {
    jasmine.clock().install();
    jasmine.clock().mockDate(new Date('2024-01-01T08:00:00'));

    expect(component.greeting).toBe('Bom dia');

    jasmine.clock().uninstall();
  });

  it('greeting deve retornar "Boa tarde" para hora entre 12 e 17', () => {
    jasmine.clock().install();
    jasmine.clock().mockDate(new Date('2024-01-01T15:00:00'));

    expect(component.greeting).toBe('Boa tarde');

    jasmine.clock().uninstall();
  });

  it('greeting deve retornar "Boa noite" para hora maior ou igual a 18', () => {
    jasmine.clock().install();
    jasmine.clock().mockDate(new Date('2024-01-01T20:00:00'));

    expect(component.greeting).toBe('Boa noite');

    jasmine.clock().uninstall();
  });

  // --- ngOnInit ---

  it('ngOnInit deve carregar stats e chamados recentes', async () => {
    const tickets = [1, 2, 3].map(makeTicket);
    apiSpy.getDashboardStats.and.returnValue(Promise.resolve(mockStats));
    apiSpy.listTickets.and.returnValue(Promise.resolve(tickets));

    await component.ngOnInit();

    expect(component.stats).toEqual(mockStats);
    expect(component.recentTickets.length).toBe(3);
  });

  it('ngOnInit deve limitar chamados recentes a 5', async () => {
    const tickets = [1, 2, 3, 4, 5, 6, 7].map(makeTicket);
    apiSpy.getDashboardStats.and.returnValue(Promise.resolve(mockStats));
    apiSpy.listTickets.and.returnValue(Promise.resolve(tickets));

    await component.ngOnInit();

    expect(component.recentTickets.length).toBe(5);
  });

  it('ngOnInit deve limpar dados quando a API falha', async () => {
    apiSpy.getDashboardStats.and.returnValue(Promise.reject(new Error('Falha')));
    apiSpy.listTickets.and.returnValue(Promise.resolve([]));

    await component.ngOnInit();

    expect(component.stats).toBeNull();
    expect(component.recentTickets).toEqual([]);
  });
});
