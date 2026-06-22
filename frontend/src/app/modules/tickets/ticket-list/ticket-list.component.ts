import { Component, inject, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { SlicePipe } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { HelpdeskApiService } from '../../../core/services/helpdesk-api.service';
import { STATUS, PRIORITY, Ticket } from '../../../core/models/ticket.model';

@Component({
  selector: 'app-ticket-list',
  standalone: true,
  imports: [RouterLink, FormsModule, SlicePipe],
  templateUrl: './ticket-list.component.html',
  styleUrl: './ticket-list.component.css',
})
export class TicketListComponent implements OnInit {
  auth = inject(AuthService);
  private api = inject(HelpdeskApiService);

  allTickets: Ticket[] = [];
  filtered: Ticket[] = [];
  status = STATUS;
  priority = PRIORITY;

  filterStatus = '';
  filterPriority = '';
  filterSearch = '';

  async ngOnInit() {
    await this.load();
  }

  async load() {
    try {
      this.allTickets = await this.api.listTickets();
      this.applyFilters();
    } catch {
      this.allTickets = [];
      this.filtered = [];
    }
  }

  applyFilters() {
    this.filtered = this.allTickets.filter(t => {
      const matchStatus = !this.filterStatus || t.status === +this.filterStatus;
      const matchPriority = !this.filterPriority || t.priority === +this.filterPriority;
      const matchSearch = !this.filterSearch || t.title.toLowerCase().includes(this.filterSearch.toLowerCase());
      return matchStatus && matchPriority && matchSearch;
    });
  }

  clearFilters() {
    this.filterStatus = '';
    this.filterPriority = '';
    this.filterSearch = '';
    this.applyFilters();
  }

  get statusKeys() { return Object.keys(STATUS).map(Number); }
  get priorityKeys() { return Object.keys(PRIORITY).map(Number); }
}
