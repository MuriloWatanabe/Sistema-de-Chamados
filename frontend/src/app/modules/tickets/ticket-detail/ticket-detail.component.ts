import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { MockDataService } from '../../../core/services/mock-data.service';
import { STATUS, PRIORITY, Ticket } from '../../../core/models/ticket.model';
import { Comment } from '../../../core/models/comment.model';
import { ROLES } from '../../../core/models/user.model';

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
  private dataService = inject(MockDataService);

  ticket: Ticket | null = null;
  comments: Comment[] = [];
  technicians = this.dataService.getTechnicianUsers();
  newComment = '';
  status = STATUS;
  priority = PRIORITY;
  roles = ROLES;
  statusKeys = Object.keys(STATUS).map(Number);

  get user() { return this.auth.currentUser!; }
  get canManage() { return this.auth.isTechnicianOrAbove(); }

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.ticket = this.dataService.getTicketById(id) ?? null;
    if (this.ticket) {
      this.comments = this.dataService.getCommentsByTicketId(id);
    }
  }

  changeStatus(newStatus: number) {
    if (!this.ticket) return;
    const updates: Partial<Ticket> = { status: newStatus };
    if (newStatus === 3) updates.closedAt = new Date().toISOString();
    this.ticket = this.dataService.updateTicket(this.ticket.id, updates);
  }

  assignTechnician(techId: string) {
    if (!this.ticket) return;
    const tech = this.technicians.find(t => t.id === +techId);
    this.ticket = this.dataService.updateTicket(this.ticket.id, {
      assignedTo: tech ? { id: tech.id, name: tech.name, email: tech.email } : undefined,
      status: tech ? 1 : this.ticket.status,
    });
  }

  submitComment() {
    if (!this.newComment.trim() || !this.ticket) return;
    const comment = this.dataService.addComment(this.ticket.id, { id: this.user.id, name: this.user.name, role: this.user.role }, this.newComment.trim());
    this.comments = [...this.comments, comment];
    this.newComment = '';
  }

  getInitials(name: string) {
    return name.split(' ').slice(0, 2).map(n => n[0]).join('').toUpperCase();
  }

  formatDate(iso: string) {
    return new Date(iso).toLocaleString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
  }
}
