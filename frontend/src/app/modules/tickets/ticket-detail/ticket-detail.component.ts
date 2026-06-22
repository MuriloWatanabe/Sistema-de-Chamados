import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { HelpdeskApiService } from '../../../core/services/helpdesk-api.service';
import { STATUS, PRIORITY, Ticket } from '../../../core/models/ticket.model';
import { Comment } from '../../../core/models/comment.model';
import { ROLES, User } from '../../../core/models/user.model';

@Component({
  selector: 'app-ticket-detail',
  standalone: true,
  imports: [RouterLink, FormsModule],
  templateUrl: './ticket-detail.component.html',
  styleUrl: './ticket-detail.component.css',
})
export class TicketDetailComponent implements OnInit {
  auth = inject(AuthService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private api = inject(HelpdeskApiService);

  ticket: Ticket | null = null;
  comments: Comment[] = [];
  technicians: User[] = [];
  newComment = '';
  status = STATUS;
  priority = PRIORITY;
  roles = ROLES;
  statusKeys = Object.keys(STATUS).map(Number);
  priorityKeys = Object.keys(PRIORITY).map(Number);
  loading = true;
  error = '';
  deleting = false;

  get user() { return this.auth.currentUser!; }
  get canManage() { return this.auth.isTechnicianOrAbove(); }
  get canDelete() { return this.auth.isAdmin(); }

  async ngOnInit() {
    await this.load();
  }

  async changeStatus(newStatus: number) {
    if (!this.ticket) return;
    this.error = '';
    try {
      this.ticket = await this.api.updateTicket(this.ticket.id, { status: newStatus });
    } catch (error) {
      this.error = error instanceof Error ? error.message : 'Nao foi possivel atualizar o status.';
    }
  }

  async changePriority(newPriority: number) {
    if (!this.ticket) return;
    this.error = '';
    try {
      this.ticket = await this.api.updateTicket(this.ticket.id, { priority: newPriority });
    } catch (error) {
      this.error = error instanceof Error ? error.message : 'Nao foi possivel atualizar a prioridade.';
    }
  }

  async assignTechnician(techId: string) {
    if (!this.ticket) return;
    const assignedToId = Number(techId);
    if (!assignedToId) return;

    this.error = '';
    try {
      this.ticket = await this.api.updateTicket(this.ticket.id, { assignedToId });
    } catch (error) {
      this.error = error instanceof Error ? error.message : 'Nao foi possivel atualizar o responsavel.';
    }
  }

  async submitComment() {
    if (!this.ticket || !this.newComment.trim()) return;

    this.error = '';
    try {
      const comment = await this.api.addComment(this.ticket.id, { comment: this.newComment.trim() });
      this.comments = [...this.comments, comment];
      this.newComment = '';
    } catch (error) {
      this.error = error instanceof Error ? error.message : 'Nao foi possivel adicionar o comentario.';
    }
  }

  async deleteTicket() {
    if (!this.ticket || this.deleting) return;
    if (!confirm(`Excluir o chamado #${this.ticket.id}?`)) return;

    this.deleting = true;
    this.error = '';
    try {
      await this.api.deleteTicket(this.ticket.id);
      await this.router.navigate(['/tickets']);
    } catch (error) {
      this.error = error instanceof Error ? error.message : 'Nao foi possivel excluir o chamado.';
    } finally {
      this.deleting = false;
    }
  }

  getInitials(name: string) {
    return name.split(' ').slice(0, 2).map(n => n[0]).join('').toUpperCase();
  }

  formatDate(iso: string) {
    return new Date(iso).toLocaleString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
  }

  private async load() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) {
      this.error = 'Chamado invalido.';
      this.loading = false;
      return;
    }

    try {
      const ticket = await this.api.getTicket(id);
      this.ticket = ticket;
      this.comments = await this.api.listComments(id);
      if (this.canManage) {
        try {
          this.technicians = await this.api.listTechnicians();
        } catch {
          this.technicians = [];
        }
      }
    } catch (error) {
      this.ticket = null;
      this.comments = [];
      this.technicians = [];
      this.error = error instanceof Error ? error.message : 'Nao foi possivel carregar o chamado.';
    } finally {
      this.loading = false;
    }
  }
}
