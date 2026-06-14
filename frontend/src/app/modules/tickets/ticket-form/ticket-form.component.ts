import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { MockDataService } from '../../../core/services/mock-data.service';
import { PRIORITY } from '../../../core/models/ticket.model';

@Component({
  selector: 'app-ticket-form',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './ticket-form.component.html',
  styleUrl: './ticket-form.component.css',
})
export class TicketFormComponent {
  auth = inject(AuthService);
  private router = inject(Router);
  private dataService = inject(MockDataService);

  priority = PRIORITY;
  priorityKeys = Object.keys(PRIORITY).map(Number);

  form = {
    title: '',
    description: '',
    priority: 1,
  };

  error = '';

  submit() {
    if (!this.form.title.trim() || !this.form.description.trim()) {
      this.error = 'Preencha todos os campos obrigatórios.';
      return;
    }
    const user = this.auth.currentUser!;
    const ticket = this.dataService.createTicket({
      title: this.form.title.trim(),
      description: this.form.description.trim(),
      status: 0,
      priority: this.form.priority,
      requester: { id: user.id, name: user.name, email: user.email },
    });
    this.router.navigate(['/tickets', ticket.id]);
  }
}
