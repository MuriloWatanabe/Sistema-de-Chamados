import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AUDIT_ENTITY_TYPES, AuditRecord } from '../../core/models/audit.model';
import { ROLES } from '../../core/models/user.model';
import { AuthService } from '../../core/services/auth.service';
import { AuditService } from '../../core/services/audit.service';

@Component({
  selector: 'app-logs',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './logs.component.html',
  styleUrl: './logs.component.css',
})
export class LogsComponent implements OnInit {
  private readonly auditService = inject(AuditService);
  auth = inject(AuthService);

  entries: AuditRecord[] = [];
  filteredEntries: AuditRecord[] = [];
  selectedEntry: AuditRecord | null = null;
  loading = false;
  error = '';

  searchTerm = '';
  entityFilter = '';

  summary = {
    total: 0,
    users: 0,
    tickets: 0,
    comments: 0,
    auth: 0,
  };

  readonly entityTypes = AUDIT_ENTITY_TYPES;
  readonly roles = ROLES;
  readonly entityKeys = Object.keys(AUDIT_ENTITY_TYPES).map(Number);

  ngOnInit(): void {
    void this.load();
  }

  async load(): Promise<void> {
    this.loading = true;
    this.error = '';

    try {
      this.entries = await this.auditService.listAudits();
      this.rebuildSummary();
      this.applyFilters();
    } catch (err) {
      this.entries = [];
      this.filteredEntries = [];
      this.selectedEntry = null;
      this.error = err instanceof Error ? err.message : 'Nao foi possivel carregar os logs.';
    } finally {
      this.loading = false;
    }
  }

  applyFilters(): void {
    const term = this.searchTerm.trim().toLowerCase();
    const entityType = this.entityFilter === '' ? null : Number(this.entityFilter);

    this.filteredEntries = this.entries.filter((entry) => {
      const matchesEntity = entityType === null || entry.entityType === entityType;
      const searchableText = [
        entry.action,
        this.formatAction(entry.action),
        entry.user?.name,
        entry.user?.email,
        this.entityLabel(entry.entityType),
        String(entry.entityId),
        this.formatJson(entry.oldValue),
        this.formatJson(entry.newValue),
      ]
        .filter(Boolean)
        .join(' ')
        .toLowerCase();

      return matchesEntity && (!term || searchableText.includes(term));
    });

    this.syncSelection();
  }

  clearFilters(): void {
    this.searchTerm = '';
    this.entityFilter = '';
    this.applyFilters();
  }

  selectEntry(entry: AuditRecord): void {
    this.selectedEntry = entry;
  }

  formatDateTime(value: string): string {
    return new Date(value).toLocaleString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  }

  formatAction(value: string): string {
    return value
      .split('_')
      .filter(Boolean)
      .map((part) => part.charAt(0) + part.slice(1).toLowerCase())
      .join(' ');
  }

  entityLabel(entityType: number): string {
    return this.entityTypes[entityType]?.label ?? `Tipo ${entityType}`;
  }

  entityStyle(entityType: number): { color: string; bg: string } {
    return this.entityTypes[entityType] ?? { color: '#475569', bg: '#E2E8F0' };
  }

  formatJson(value: unknown): string {
    if (value === null || value === undefined) {
      return '-';
    }

    if (typeof value === 'string') {
      return value;
    }

    return JSON.stringify(value, null, 2);
  }

  getInitials(name: string): string {
    return name
      .split(' ')
      .slice(0, 2)
      .map((part) => part[0])
      .join('')
      .toUpperCase();
  }

  private rebuildSummary(): void {
    this.summary = {
      total: this.entries.length,
      users: this.entries.filter((entry) => entry.entityType === 0).length,
      tickets: this.entries.filter((entry) => entry.entityType === 1).length,
      comments: this.entries.filter((entry) => entry.entityType === 2).length,
      auth: this.entries.filter((entry) => entry.entityType === 3).length,
    };
  }

  private syncSelection(): void {
    if (this.filteredEntries.length === 0) {
      this.selectedEntry = null;
      return;
    }

    const selectedId = this.selectedEntry?.id;
    if (selectedId) {
      const stillVisible = this.filteredEntries.find((entry) => entry.id === selectedId);
      if (stillVisible) {
        this.selectedEntry = stillVisible;
        return;
      }
    }

    this.selectedEntry = this.filteredEntries[0];
  }
}
