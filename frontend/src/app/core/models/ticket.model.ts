export interface Ticket {
  id: number;
  title: string;
  description: string;
  status: number;
  priority: number;
  requester: { id: number; name: string; email: string };
  assignedTo?: { id: number; name: string; email: string };
  createdAt: string;
  updatedAt: string;
  closedAt?: string;
}

export const STATUS: Record<number, { label: string; color: string; bg: string }> = {
  0: { label: 'Aberto', color: '#1D4ED8', bg: '#DBEAFE' },
  1: { label: 'Em Andamento', color: '#B45309', bg: '#FEF3C7' },
  2: { label: 'Resolvido', color: '#065F46', bg: '#D1FAE5' },
  3: { label: 'Fechado', color: '#374151', bg: '#F3F4F6' },
};

export const PRIORITY: Record<number, { label: string; color: string; bg: string }> = {
  0: { label: 'Baixa', color: '#065F46', bg: '#D1FAE5' },
  1: { label: 'Média', color: '#B45309', bg: '#FEF3C7' },
  2: { label: 'Alta', color: '#991B1B', bg: '#FEE2E2' },
  3: { label: 'Urgente', color: '#5B21B6', bg: '#EDE9FE' },
};
