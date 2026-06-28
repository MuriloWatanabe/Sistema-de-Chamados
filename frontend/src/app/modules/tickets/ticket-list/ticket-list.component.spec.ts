import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { TicketListComponent } from './ticket-list.component';
import { AuthService } from '../../../core/services/auth.service';
import { HelpdeskApiService } from '../../../core/services/helpdesk-api.service';
import { Ticket } from '../../../core/models/ticket.model';

const makeTicket = (id: number, status: number, priority: number, title = `Ticket ${id}`): Ticket => ({
  id, title, description: 'Desc', status, priority,
  requester: { id: 1, name: 'User', email: 'u@test.com' },
  createdAt: '', updatedAt: '',
});

describe('TicketListComponent', () => {
  let component: TicketListComponent;
  let apiSpy: jasmine.SpyObj<HelpdeskApiService>;
  let authSpy: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    apiSpy = jasmine.createSpyObj('HelpdeskApiService', ['listTickets']);
    authSpy = jasmine.createSpyObj('AuthService', ['isTechnicianOrAbove', 'isAdmin']);

    await TestBed.configureTestingModule({
      imports: [TicketListComponent],
      providers: [
        { provide: HelpdeskApiService, useValue: apiSpy },
        { provide: AuthService, useValue: authSpy },
        { provide: Router, useValue: jasmine.createSpyObj('Router', ['navigate']) },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(TicketListComponent);
    component = fixture.componentInstance;
  });

  // --- applyFilters: sem filtros ---

  it('deve exibir todos os chamados quando nenhum filtro esta ativo', () => {
    component.allTickets = [
      makeTicket(1, 0, 1),
      makeTicket(2, 1, 2),
      makeTicket(3, 2, 0),
    ];

    component.applyFilters();

    expect(component.filtered.length).toBe(3);
  });

  // --- applyFilters: por status ---

  it('deve filtrar chamados pelo status', () => {
    component.allTickets = [
      makeTicket(1, 0, 1),
      makeTicket(2, 1, 1),
      makeTicket(3, 0, 2),
    ];
    component.filterStatus = '0';

    component.applyFilters();

    expect(component.filtered.length).toBe(2);
    expect(component.filtered.every(t => t.status === 0)).toBeTrue();
  });

  // --- applyFilters: por prioridade ---

  it('deve filtrar chamados pela prioridade', () => {
    component.allTickets = [
      makeTicket(1, 0, 2),
      makeTicket(2, 1, 1),
      makeTicket(3, 2, 2),
    ];
    component.filterPriority = '2';

    component.applyFilters();

    expect(component.filtered.length).toBe(2);
    expect(component.filtered.every(t => t.priority === 2)).toBeTrue();
  });

  // --- applyFilters: por titulo ---

  it('deve filtrar chamados pelo titulo (case-insensitive)', () => {
    component.allTickets = [
      makeTicket(1, 0, 1, 'Problema de acesso'),
      makeTicket(2, 0, 1, 'Impressora com defeito'),
      makeTicket(3, 0, 1, 'Problema de rede'),
    ];
    component.filterSearch = 'problema';

    component.applyFilters();

    expect(component.filtered.length).toBe(2);
    expect(component.filtered.map(t => t.id)).toEqual([1, 3]);
  });

  it('deve filtrar por titulo com letras maiusculas na busca', () => {
    component.allTickets = [makeTicket(1, 0, 1, 'Acesso negado')];
    component.filterSearch = 'ACESSO';

    component.applyFilters();

    expect(component.filtered.length).toBe(1);
  });

  // --- applyFilters: multiplos filtros ---

  it('deve aplicar status e prioridade simultaneamente', () => {
    component.allTickets = [
      makeTicket(1, 0, 1),
      makeTicket(2, 0, 2),
      makeTicket(3, 1, 1),
    ];
    component.filterStatus = '0';
    component.filterPriority = '1';

    component.applyFilters();

    expect(component.filtered.length).toBe(1);
    expect(component.filtered[0].id).toBe(1);
  });

  it('deve retornar lista vazia quando nenhum chamado corresponde aos filtros', () => {
    component.allTickets = [makeTicket(1, 0, 1)];
    component.filterStatus = '3';

    component.applyFilters();

    expect(component.filtered.length).toBe(0);
  });

  // --- clearFilters ---

  it('clearFilters deve redefinir todos os filtros', () => {
    component.filterStatus = '1';
    component.filterPriority = '2';
    component.filterSearch = 'busca';
    component.allTickets = [makeTicket(1, 0, 0)];

    component.clearFilters();

    expect(component.filterStatus).toBe('');
    expect(component.filterPriority).toBe('');
    expect(component.filterSearch).toBe('');
  });

  it('clearFilters deve reexibir todos os chamados', () => {
    component.allTickets = [makeTicket(1, 0, 1), makeTicket(2, 1, 2)];
    component.filterStatus = '0';
    component.applyFilters();
    expect(component.filtered.length).toBe(1);

    component.clearFilters();

    expect(component.filtered.length).toBe(2);
  });

  // --- statusKeys / priorityKeys ---

  it('statusKeys deve conter as chaves numericas dos status', () => {
    expect(component.statusKeys).toEqual(jasmine.arrayContaining([0, 1, 2, 3]));
  });

  it('priorityKeys deve conter as chaves numericas das prioridades', () => {
    expect(component.priorityKeys).toEqual(jasmine.arrayContaining([0, 1, 2, 3]));
  });

  // --- load ---

  it('load deve preencher allTickets a partir da API', async () => {
    const tickets = [makeTicket(1, 0, 1), makeTicket(2, 1, 2)];
    apiSpy.listTickets.and.returnValue(Promise.resolve(tickets));

    await component.load();

    expect(component.allTickets.length).toBe(2);
  });

  it('load deve zerar listas quando a API falha', async () => {
    apiSpy.listTickets.and.returnValue(Promise.reject(new Error('Falha')));

    await component.load();

    expect(component.allTickets).toEqual([]);
    expect(component.filtered).toEqual([]);
  });
});
