import { Injectable } from '@angular/core';
import { ApiClientService } from './api-client.service';
import {
  CommentCreateRequest,
  DashboardStats,
  TicketCreateRequest,
  TicketUpdateRequest,
  UserUpsertRequest,
} from '../models/api-contracts';
import { Comment } from '../models/comment.model';
import { Ticket } from '../models/ticket.model';
import { User } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class HelpdeskApiService {
  constructor(private api: ApiClientService) {}

  getDashboardStats(): Promise<DashboardStats> {
    return this.api.get<DashboardStats>('/dashboard/stats');
  }

  listTickets(): Promise<Ticket[]> {
    return this.api.get<Ticket[]>('/tickets');
  }

  getTicket(id: number): Promise<Ticket> {
    return this.api.get<Ticket>(`/tickets/${id}`);
  }

  createTicket(request: TicketCreateRequest): Promise<Ticket> {
    return this.api.post<Ticket>('/tickets', request);
  }

  updateTicket(id: number, request: TicketUpdateRequest): Promise<Ticket> {
    return this.api.patch<Ticket>(`/tickets/${id}`, request);
  }

  deleteTicket(id: number): Promise<void> {
    return this.api.delete<void>(`/tickets/${id}`);
  }

  listComments(ticketId: number): Promise<Comment[]> {
    return this.api.get<Comment[]>(`/tickets/${ticketId}/comments`);
  }

  addComment(ticketId: number, request: CommentCreateRequest): Promise<Comment> {
    return this.api.post<Comment>(`/tickets/${ticketId}/comments`, request);
  }

  listUsers(): Promise<User[]> {
    return this.api.get<User[]>('/users');
  }

  listTechnicians(): Promise<User[]> {
    return this.api.get<User[]>('/users/technicians');
  }

  createUser(request: UserUpsertRequest): Promise<User> {
    return this.api.post<User>('/users', request);
  }

  updateUser(id: number, request: UserUpsertRequest): Promise<User> {
    return this.api.put<User>(`/users/${id}`, request);
  }

  deleteUser(id: number): Promise<void> {
    return this.api.delete<void>(`/users/${id}`);
  }
}
