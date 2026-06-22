import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HelpdeskApiService } from '../../../core/services/helpdesk-api.service';
import { PRIORITY } from '../../../core/models/ticket.model';

@Component({
  selector: 'app-ticket-form',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './ticket-form.component.html',
  styleUrl: './ticket-form.component.css',
})
export class TicketFormComponent {
  private router = inject(Router);
  private api = inject(HelpdeskApiService);

  priority = PRIORITY;
  priorityKeys = Object.keys(PRIORITY).map(Number);

  form = {
    title: '',
    description: '',
    priority: 1,
  };

  error = '';

  async submit() {
    if (!this.form.title.trim() || !this.form.description.trim()) {
      this.error = 'Preencha todos os campos obrigatorios.';
      return;
    }

    this.error = '';

    try {
      const ticket = await this.api.createTicket({
        title: this.form.title.trim(),
        description: this.form.description.trim(),
        priority: Number(this.form.priority),
      });
      this.router.navigate(['/tickets', ticket.id]);
    } catch (error) {
      this.error = error instanceof Error ? error.message : 'Nao foi possivel abrir o chamado.';
    }
  }
}
