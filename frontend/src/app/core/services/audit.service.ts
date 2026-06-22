import { Injectable } from '@angular/core';
import { AuditRecord } from '../models/audit.model';
import { ApiClientService } from './api-client.service';

@Injectable({ providedIn: 'root' })
export class AuditService {
  constructor(private api: ApiClientService) {}

  async listAudits(): Promise<AuditRecord[]> {
    return this.api.get<AuditRecord[]>('/audits');
  }
}
