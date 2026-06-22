export interface AuditUserSummary {
  id: number;
  name: string;
  email: string;
  role: number;
}

export interface AuditRecord {
  id: number;
  user: AuditUserSummary;
  action: string;
  entityType: number;
  entityId: number;
  oldValue: unknown;
  newValue: unknown;
  createdAt: string;
}

export const AUDIT_ENTITY_TYPES: Record<number, { label: string; color: string; bg: string }> = {
  0: { label: 'Usuario', color: '#5B21B6', bg: '#EDE9FE' },
  1: { label: 'Chamado', color: '#1D4ED8', bg: '#DBEAFE' },
  2: { label: 'Comentario', color: '#0E7490', bg: '#CFFAFE' },
  3: { label: 'Autenticacao', color: '#92400E', bg: '#FEF3C7' },
};
