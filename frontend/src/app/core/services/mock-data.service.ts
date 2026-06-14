import { Injectable } from '@angular/core';
import { User } from '../models/user.model';
import { Ticket } from '../models/ticket.model';
import { Comment } from '../models/comment.model';

export const MOCK_USERS: User[] = [
  { id: 1, name: 'Admin Sistema', email: 'admin@helpdesk.com', active: true, role: 0, createdAt: '2024-01-01T00:00:00', updatedAt: '2024-01-01T00:00:00' },
  { id: 2, name: 'Carlos Supervisor', email: 'supervisor@helpdesk.com', active: true, role: 1, createdAt: '2024-01-02T00:00:00', updatedAt: '2024-01-02T00:00:00' },
  { id: 3, name: 'Ana Técnica', email: 'tecnico@helpdesk.com', active: true, role: 2, createdAt: '2024-01-03T00:00:00', updatedAt: '2024-01-03T00:00:00' },
  { id: 4, name: 'João Solicitante', email: 'solicitante@helpdesk.com', active: true, role: 3, createdAt: '2024-01-04T00:00:00', updatedAt: '2024-01-04T00:00:00' },
  { id: 5, name: 'Maria Santos', email: 'maria@helpdesk.com', active: true, role: 3, createdAt: '2024-01-05T00:00:00', updatedAt: '2024-01-05T00:00:00' },
];

export const CREDENTIALS: Record<string, string> = {
  'admin@helpdesk.com': 'admin123',
  'supervisor@helpdesk.com': 'super123',
  'tecnico@helpdesk.com': 'tecnico123',
  'solicitante@helpdesk.com': 'solicitante123',
};

const INITIAL_TICKETS: Ticket[] = [
  {
    id: 1, title: 'Computador não liga', description: 'Meu computador não está ligando desde ontem. Já verifiquei o cabo de energia e está tudo conectado corretamente.',
    status: 0, priority: 2,
    requester: { id: 4, name: 'João Solicitante', email: 'solicitante@helpdesk.com' },
    createdAt: '2024-06-10T09:00:00', updatedAt: '2024-06-10T09:00:00',
  },
  {
    id: 2, title: 'Sem acesso ao sistema ERP', description: 'Não consigo fazer login no sistema ERP. Recebo a mensagem "Usuário não encontrado" ao tentar entrar.',
    status: 1, priority: 2,
    requester: { id: 4, name: 'João Solicitante', email: 'solicitante@helpdesk.com' },
    assignedTo: { id: 3, name: 'Ana Técnica', email: 'tecnico@helpdesk.com' },
    createdAt: '2024-06-09T14:30:00', updatedAt: '2024-06-09T16:00:00',
  },
  {
    id: 3, title: 'Impressora offline no setor financeiro', description: 'A impressora HP LaserJet do setor financeiro está offline e não está imprimindo nenhum documento.',
    status: 2, priority: 1,
    requester: { id: 5, name: 'Maria Santos', email: 'maria@helpdesk.com' },
    assignedTo: { id: 3, name: 'Ana Técnica', email: 'tecnico@helpdesk.com' },
    createdAt: '2024-06-08T10:00:00', updatedAt: '2024-06-10T11:00:00',
  },
  {
    id: 4, title: 'VPN não conecta ao trabalhar remoto', description: 'Estou tentando me conectar à VPN da empresa para trabalho remoto, mas recebo erro de timeout após 30 segundos.',
    status: 0, priority: 3,
    requester: { id: 5, name: 'Maria Santos', email: 'maria@helpdesk.com' },
    createdAt: '2024-06-10T08:00:00', updatedAt: '2024-06-10T08:00:00',
  },
  {
    id: 5, title: 'Atualização do Windows pendente', description: 'Meu computador está solicitando atualização do Windows mas não sei se posso instalar durante o horário de trabalho.',
    status: 3, priority: 0,
    requester: { id: 4, name: 'João Solicitante', email: 'solicitante@helpdesk.com' },
    assignedTo: { id: 3, name: 'Ana Técnica', email: 'tecnico@helpdesk.com' },
    createdAt: '2024-06-05T09:00:00', updatedAt: '2024-06-07T10:00:00', closedAt: '2024-06-07T10:00:00',
  },
];

const INITIAL_COMMENTS: Comment[] = [
  { id: 1, ticketId: 2, user: { id: 3, name: 'Ana Técnica', role: 2 }, comment: 'Já identifiquei o problema. O usuário foi bloqueado por tentativas incorretas de login. Vou desbloquear agora.', createdAt: '2024-06-09T15:00:00' },
  { id: 2, ticketId: 2, user: { id: 4, name: 'João Solicitante', role: 3 }, comment: 'Obrigado pela rápida resposta! Quando isso estará resolvido?', createdAt: '2024-06-09T15:30:00' },
  { id: 3, ticketId: 3, user: { id: 3, name: 'Ana Técnica', role: 2 }, comment: 'Impressora foi reiniciada e reconectada na rede. Teste de impressão realizado com sucesso.', createdAt: '2024-06-10T10:30:00' },
  { id: 4, ticketId: 3, user: { id: 5, name: 'Maria Santos', role: 3 }, comment: 'Perfeito! Já está funcionando. Muito obrigada!', createdAt: '2024-06-10T10:45:00' },
];

@Injectable({ providedIn: 'root' })
export class MockDataService {
  private users = [...MOCK_USERS];
  private tickets = [...INITIAL_TICKETS];
  private comments = [...INITIAL_COMMENTS];
  private nextTicketId = 6;
  private nextCommentId = 5;
  private nextUserId = 6;

  getUsers(): User[] { return [...this.users]; }

  getUserById(id: number): User | undefined { return this.users.find(u => u.id === id); }

  getTickets(): Ticket[] { return [...this.tickets].sort((a, b) => b.id - a.id); }

  getTicketsByRequesterId(requesterId: number): Ticket[] {
    return this.tickets.filter(t => t.requester.id === requesterId).sort((a, b) => b.id - a.id);
  }

  getTicketById(id: number): Ticket | undefined { return this.tickets.find(t => t.id === id); }

  getCommentsByTicketId(ticketId: number): Comment[] {
    return this.comments.filter(c => c.ticketId === ticketId).sort((a, b) => a.id - b.id);
  }

  createTicket(data: Omit<Ticket, 'id' | 'createdAt' | 'updatedAt'>): Ticket {
    const now = new Date().toISOString();
    const ticket: Ticket = { ...data, id: this.nextTicketId++, createdAt: now, updatedAt: now };
    this.tickets.unshift(ticket);
    return ticket;
  }

  updateTicket(id: number, updates: Partial<Ticket>): Ticket | null {
    const idx = this.tickets.findIndex(t => t.id === id);
    if (idx === -1) return null;
    this.tickets[idx] = { ...this.tickets[idx], ...updates, updatedAt: new Date().toISOString() };
    return { ...this.tickets[idx] };
  }

  addComment(ticketId: number, user: { id: number; name: string; role: number }, text: string): Comment {
    const comment: Comment = { id: this.nextCommentId++, ticketId, user, comment: text, createdAt: new Date().toISOString() };
    this.comments.push(comment);
    return comment;
  }

  createUser(data: Omit<User, 'id' | 'createdAt' | 'updatedAt'>): User {
    const now = new Date().toISOString();
    const user: User = { ...data, id: this.nextUserId++, createdAt: now, updatedAt: now };
    this.users.push(user);
    return user;
  }

  updateUser(id: number, updates: Partial<User>): User | null {
    const idx = this.users.findIndex(u => u.id === id);
    if (idx === -1) return null;
    this.users[idx] = { ...this.users[idx], ...updates, updatedAt: new Date().toISOString() };
    return { ...this.users[idx] };
  }

  deleteUser(id: number): boolean {
    const idx = this.users.findIndex(u => u.id === id);
    if (idx === -1) return false;
    this.users.splice(idx, 1);
    return true;
  }

  getDashboardStats() {
    return {
      total: this.tickets.length,
      open: this.tickets.filter(t => t.status === 0).length,
      inProgress: this.tickets.filter(t => t.status === 1).length,
      resolved: this.tickets.filter(t => t.status === 2).length,
      closed: this.tickets.filter(t => t.status === 3).length,
      urgent: this.tickets.filter(t => t.priority === 3 && t.status < 3).length,
    };
  }

  getTechnicianUsers(): User[] { return this.users.filter(u => u.role === 2 && u.active); }
}
