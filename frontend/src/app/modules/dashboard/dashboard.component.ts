import { Component, inject, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { SlicePipe } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';
import { HelpdeskApiService } from '../../core/services/helpdesk-api.service';
import { DashboardStats } from '../../core/models/api-contracts';
import { PRIORITY, STATUS, Ticket } from '../../core/models/ticket.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterLink, SlicePipe],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css',
})
export class DashboardComponent implements OnInit {
  auth = inject(AuthService);
  private api = inject(HelpdeskApiService);

  stats: DashboardStats | null = null;
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

  async ngOnInit() {
    try {
      const [stats, tickets] = await Promise.all([
        this.api.getDashboardStats(),
        this.api.listTickets(),
      ]);
      this.stats = stats;
      this.recentTickets = tickets.slice(0, 5);
    } catch {
      this.stats = null;
      this.recentTickets = [];
    }
  }
}
