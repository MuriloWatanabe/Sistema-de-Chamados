import { TestBed } from '@angular/core/testing';
import { AuditService } from './audit.service';
import { ApiClientService } from './api-client.service';
import { AuditRecord } from '../models/audit.model';

describe('AuditService', () => {
  let service: AuditService;
  let apiSpy: jasmine.SpyObj<ApiClientService>;

  const mockAudit: AuditRecord = {
    id: 1,
    user: { id: 1, name: 'Admin', email: 'admin@test.com', role: 0 },
    action: 'USER_CREATED',
    entityType: 0,
    entityId: 5,
    oldValue: null,
    newValue: null,
    createdAt: '2024-01-01T00:00:00',
  };

  beforeEach(() => {
    apiSpy = jasmine.createSpyObj('ApiClientService', ['get']);

    TestBed.configureTestingModule({
      providers: [
        AuditService,
        { provide: ApiClientService, useValue: apiSpy },
      ],
    });

    service = TestBed.inject(AuditService);
  });

  it('listAudits deve chamar GET /audits', async () => {
    apiSpy.get.and.returnValue(Promise.resolve([mockAudit]));

    const result = await service.listAudits();

    expect(apiSpy.get).toHaveBeenCalledWith('/audits');
    expect(result).toEqual([mockAudit]);
  });

  it('listAudits deve retornar lista vazia quando nao ha registros', async () => {
    apiSpy.get.and.returnValue(Promise.resolve([]));

    const result = await service.listAudits();

    expect(result).toEqual([]);
  });

  it('listAudits deve propagar erro quando a API falha', async () => {
    apiSpy.get.and.returnValue(Promise.reject(new Error('Falha de rede')));

    await expectAsync(service.listAudits()).toBeRejectedWithError('Falha de rede');
  });
});
