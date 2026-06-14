import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
})
export class LoginComponent {
  private auth = inject(AuthService);
  private router = inject(Router);

  email = '';
  password = '';
  error = signal('');
  loading = signal(false);

  readonly hints = [
    { role: 'Administrador', email: 'admin@helpdesk.com', pass: 'admin123' },
    { role: 'Supervisor', email: 'supervisor@helpdesk.com', pass: 'super123' },
    { role: 'Técnico', email: 'tecnico@helpdesk.com', pass: 'tecnico123' },
    { role: 'Solicitante', email: 'solicitante@helpdesk.com', pass: 'solicitante123' },
  ];

  fillCredentials(email: string, pass: string) {
    this.email = email;
    this.password = pass;
  }

  onSubmit() {
    this.error.set('');
    this.loading.set(true);
    setTimeout(() => {
      const ok = this.auth.login(this.email, this.password);
      this.loading.set(false);
      if (ok) {
        this.router.navigate(['/dashboard']);
      } else {
        this.error.set('E-mail ou senha incorretos.');
      }
    }, 400);
  }
}
