import { Component, inject, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { SlicePipe } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';
import { MockDataService } from '../../core/services/mock-data.service';
import { STATUS, PRIORITY, Ticket } from '../../core/models/ticket.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterLink, SlicePipe],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css',
})
export class DashboardComponent implements OnInit {
  auth = inject(AuthService);
  private data = inject(MockDataService);

  stats: { total: number; open: number; inProgress: number; resolved: number; closed: number; urgent: number } | null = null;
  recentTickets: Ticket[] = [];
  status = STATUS;
  priority = PRIORITY;

  get user() { return this.auth.currentUser; }
  get firstName() { return this.auth.currentUser?.name?.split(' ')[0] ?? ''; }
  get greeting() {
    const h = new Date().getHours();
    if (h < 12) return 'Bom dia';
    if (h < 18) return 'Boa tarde';
    return 'Boa noite';
  }

  ngOnInit() {
    this.stats = this.data.getDashboardStats();
    const tickets = this.auth.isTechnicianOrAbove()
      ? this.data.getTickets()
      : this.data.getTicketsByRequesterId(this.user!.id);
    this.recentTickets = tickets.slice(0, 5);
  }
}
