import { TestBed } from '@angular/core/testing';
import { HelpdeskApiService } from './helpdesk-api.service';
import { ApiClientService } from './api-client.service';
import { Ticket } from '../models/ticket.model';
import { User } from '../models/user.model';
import { DashboardStats } from '../models/api-contracts';

describe('HelpdeskApiService', () => {
  let service: HelpdeskApiService;
  let apiSpy: jasmine.SpyObj<ApiClientService>;

  beforeEach(() => {
    apiSpy = jasmine.createSpyObj('ApiClientService', ['get', 'post', 'put', 'patch', 'delete']);

    TestBed.configureTestingModule({
      providers: [
        HelpdeskApiService,
        { provide: ApiClientService, useValue: apiSpy },
      ],
    });

    service = TestBed.inject(HelpdeskApiService);
  });

  const mockStats: DashboardStats = { total: 10, open: 3, inProgress: 2, resolved: 4, closed: 1, urgent: 1 };
  const mockTicket: Ticket = {
    id: 1, title: 'Ticket 1', description: 'Desc', status: 0, priority: 1,
    requester: { id: 1, name: 'User', email: 'u@test.com' },
    createdAt: '', updatedAt: '',
  };
  const mockUser: User = { id: 1, name: 'User', email: 'u@test.com', active: true, role: 3, createdAt: '', updatedAt: '' };

  // --- Dashboard ---

  it('getDashboardStats deve chamar GET /dashboard/stats', async () => {
    apiSpy.get.and.returnValue(Promise.resolve(mockStats));

    const result = await service.getDashboardStats();

    expect(apiSpy.get).toHaveBeenCalledWith('/dashboard/stats');
    expect(result).toEqual(mockStats);
  });

  // --- Tickets ---

  it('listTickets deve chamar GET /tickets', async () => {
    apiSpy.get.and.returnValue(Promise.resolve([mockTicket]));

    const result = await service.listTickets();

    expect(apiSpy.get).toHaveBeenCalledWith('/tickets');
    expect(result.length).toBe(1);
  });

  it('getTicket deve chamar GET /tickets/:id', async () => {
    apiSpy.get.and.returnValue(Promise.resolve(mockTicket));

    const result = await service.getTicket(1);

    expect(apiSpy.get).toHaveBeenCalledWith('/tickets/1');
    expect(result.id).toBe(1);
  });

  it('createTicket deve chamar POST /tickets com payload', async () => {
    const request = { title: 'Novo', description: 'Desc', priority: 1 };
    apiSpy.post.and.returnValue(Promise.resolve(mockTicket));

    const result = await service.createTicket(request);

    expect(apiSpy.post).toHaveBeenCalledWith('/tickets', request);
    expect(result).toEqual(mockTicket);
  });

  it('updateTicket deve chamar PATCH /tickets/:id com payload', async () => {
    const update = { status: 2 };
    apiSpy.patch.and.returnValue(Promise.resolve(mockTicket));

    await service.updateTicket(1, update);

    expect(apiSpy.patch).toHaveBeenCalledWith('/tickets/1', update);
  });

  it('deleteTicket deve chamar DELETE /tickets/:id', async () => {
    apiSpy.delete.and.returnValue(Promise.resolve(undefined));

    await service.deleteTicket(1);

    expect(apiSpy.delete).toHaveBeenCalledWith('/tickets/1');
  });

  // --- Comentarios ---

  it('listComments deve chamar GET /tickets/:id/comments', async () => {
    apiSpy.get.and.returnValue(Promise.resolve([]));

    await service.listComments(1);

    expect(apiSpy.get).toHaveBeenCalledWith('/tickets/1/comments');
  });

  it('addComment deve chamar POST /tickets/:id/comments com payload', async () => {
    const commentMock = { id: 1, ticketId: 1, user: { id: 1, name: 'U', email: 'u@t.com', role: 3 }, comment: 'ok', createdAt: '' };
    apiSpy.post.and.returnValue(Promise.resolve(commentMock));

    await service.addComment(1, { comment: 'ok' });

    expect(apiSpy.post).toHaveBeenCalledWith('/tickets/1/comments', { comment: 'ok' });
  });

  // --- Usuarios ---

  it('listUsers deve chamar GET /users', async () => {
    apiSpy.get.and.returnValue(Promise.resolve([mockUser]));

    const result = await service.listUsers();

    expect(apiSpy.get).toHaveBeenCalledWith('/users');
    expect(result.length).toBe(1);
  });

  it('listTechnicians deve chamar GET /users/technicians', async () => {
    apiSpy.get.and.returnValue(Promise.resolve([mockUser]));

    await service.listTechnicians();

    expect(apiSpy.get).toHaveBeenCalledWith('/users/technicians');
  });

  it('createUser deve chamar POST /users com payload', async () => {
    const request = { name: 'Novo', email: 'novo@test.com', role: 3, active: true };
    apiSpy.post.and.returnValue(Promise.resolve(mockUser));

    await service.createUser(request);

    expect(apiSpy.post).toHaveBeenCalledWith('/users', request);
  });

  it('updateUser deve chamar PUT /users/:id com payload', async () => {
    const request = { name: 'Atualizado', email: 'u@test.com', role: 3, active: true };
    apiSpy.put.and.returnValue(Promise.resolve(mockUser));

    await service.updateUser(1, request);

    expect(apiSpy.put).toHaveBeenCalledWith('/users/1', request);
  });

  it('deleteUser deve chamar DELETE /users/:id', async () => {
    apiSpy.delete.and.returnValue(Promise.resolve(undefined));

    await service.deleteUser(1);

    expect(apiSpy.delete).toHaveBeenCalledWith('/users/1');
  });
});
