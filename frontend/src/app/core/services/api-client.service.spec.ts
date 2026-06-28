import { TestBed } from '@angular/core/testing';
import { ApiClientService } from './api-client.service';
import { AuthService } from './auth.service';

describe('ApiClientService', () => {
  let service: ApiClientService;
  let authSpy: jasmine.SpyObj<AuthService>;

  beforeEach(() => {
    authSpy = jasmine.createSpyObj('AuthService', ['authHeaders']);
    authSpy.authHeaders.and.returnValue({ Authorization: 'Bearer test-token' });

    TestBed.configureTestingModule({
      providers: [
        ApiClientService,
        { provide: AuthService, useValue: authSpy },
      ],
    });

    service = TestBed.inject(ApiClientService);
  });

  function mockFetch(body: unknown, status = 200): jasmine.Spy {
    const text = typeof body === 'string' ? body : JSON.stringify(body);
    return spyOn(globalThis, 'fetch').and.returnValue(
      Promise.resolve(new Response(text, { status }))
    );
  }

  // --- get ---

  it('deve chamar fetch com metodo GET', async () => {
    const fetchSpy = mockFetch({ data: 'ok' });

    await service.get('/tickets');

    expect(fetchSpy).toHaveBeenCalledOnceWith(
      'http://localhost:8080/api/tickets',
      jasmine.objectContaining({ method: 'GET' })
    );
  });

  it('deve incluir cabecalho Authorization na requisicao GET', async () => {
    const fetchSpy = mockFetch([]);

    await service.get('/tickets');

    const [, options] = fetchSpy.calls.mostRecent().args;
    expect((options as RequestInit).headers).toEqual(
      jasmine.objectContaining({ Authorization: 'Bearer test-token' })
    );
  });

  it('deve retornar o corpo parseado como JSON', async () => {
    mockFetch({ id: 1, title: 'Chamado teste' });

    const result = await service.get<{ id: number; title: string }>('/tickets/1');

    expect(result.id).toBe(1);
    expect(result.title).toBe('Chamado teste');
  });

  it('deve retornar undefined quando a resposta e vazia', async () => {
    mockFetch('');

    const result = await service.get<void>('/tickets/1');

    expect(result).toBeUndefined();
  });

  // --- post ---

  it('deve chamar fetch com metodo POST e corpo JSON', async () => {
    const fetchSpy = mockFetch({ id: 10 }, 201);
    const payload = { title: 'Novo chamado', description: 'Desc', priority: 1 };

    await service.post('/tickets', payload);

    const [, options] = fetchSpy.calls.mostRecent().args;
    expect((options as RequestInit).method).toBe('POST');
    expect((options as RequestInit).body).toBe(JSON.stringify(payload));
    expect(((options as RequestInit).headers as Record<string, string>)['Content-Type']).toBe('application/json');
  });

  // --- put ---

  it('deve chamar fetch com metodo PUT', async () => {
    const fetchSpy = mockFetch({ id: 1 });

    await service.put('/users/1', { name: 'Novo Nome' });

    const [, options] = fetchSpy.calls.mostRecent().args;
    expect((options as RequestInit).method).toBe('PUT');
  });

  // --- patch ---

  it('deve chamar fetch com metodo PATCH', async () => {
    const fetchSpy = mockFetch({ id: 1 });

    await service.patch('/tickets/1', { status: 2 });

    const [, options] = fetchSpy.calls.mostRecent().args;
    expect((options as RequestInit).method).toBe('PATCH');
  });

  // --- delete ---

  it('deve chamar fetch com metodo DELETE sem corpo', async () => {
    const fetchSpy = mockFetch('');

    await service.delete('/tickets/1');

    const [, options] = fetchSpy.calls.mostRecent().args;
    expect((options as RequestInit).method).toBe('DELETE');
    expect((options as RequestInit).body).toBeUndefined();
  });

  // --- tratamento de erros ---

  it('deve lancar erro quando a resposta nao e ok', async () => {
    mockFetch('{"message":"Ticket nao encontrado"}', 404);

    await expectAsync(service.get('/tickets/999')).toBeRejectedWithError('Ticket nao encontrado');
  });

  it('deve retornar "Sessao expirada" para erros 401 com corpo vazio', async () => {
    mockFetch('', 401);

    await expectAsync(service.get('/tickets')).toBeRejectedWithError('Sessao expirada.');
  });

  it('deve usar mensagem generica quando corpo do erro nao e JSON', async () => {
    mockFetch('Internal Server Error', 500);

    await expectAsync(service.get('/tickets')).toBeRejectedWithError('Internal Server Error');
  });

  it('deve usar campo error quando message nao esta presente no JSON de erro', async () => {
    mockFetch('{"error":"Acesso negado"}', 403);

    await expectAsync(service.get('/restricted')).toBeRejectedWithError('Acesso negado');
  });

  it('deve usar mensagem generica para status sem corpo', async () => {
    mockFetch('', 500);

    await expectAsync(service.get('/tickets')).toBeRejectedWithError('Requisicao falhou (500).');
  });
});
