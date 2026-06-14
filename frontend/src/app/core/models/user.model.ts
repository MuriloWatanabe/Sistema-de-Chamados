export interface User {
  id: number;
  name: string;
  email: string;
  active: boolean;
  role: number;
  createdAt: string;
  updatedAt: string;
}

export const ROLES: Record<number, { label: string; color: string; bg: string }> = {
  0: { label: 'Administrador', color: '#5B21B6', bg: '#EDE9FE' },
  1: { label: 'Supervisor', color: '#1D4ED8', bg: '#DBEAFE' },
  2: { label: 'Técnico', color: '#0E7490', bg: '#CFFAFE' },
  3: { label: 'Solicitante', color: '#3F6212', bg: '#ECFCCB' },
};
