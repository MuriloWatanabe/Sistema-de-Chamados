import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { SlicePipe } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { HelpdeskApiService } from '../../../core/services/helpdesk-api.service';
import { User, ROLES } from '../../../core/models/user.model';

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [FormsModule, SlicePipe],
  templateUrl: './user-list.component.html',
  styleUrl: './user-list.component.css',
})
export class UserListComponent implements OnInit {
  auth = inject(AuthService);
  private api = inject(HelpdeskApiService);

  users: User[] = [];
  roles = ROLES;
  roleKeys = Object.keys(ROLES).map(Number);

  showModal = signal(false);
  editingUser = signal<User | null>(null);

  form = { name: '', email: '', password: '', role: 3, active: true };
  formError = '';

  async ngOnInit() {
    await this.load();
  }

  async load() {
    try {
      this.users = await this.api.listUsers();
    } catch {
      this.users = [];
    }
  }

  openCreate() {
    this.editingUser.set(null);
    this.form = { name: '', email: '', password: '', role: 3, active: true };
    this.formError = '';
    this.showModal.set(true);
  }

  openEdit(user: User) {
    this.editingUser.set(user);
    this.form = { name: user.name, email: user.email, password: '', role: user.role, active: user.active };
    this.formError = '';
    this.showModal.set(true);
  }

  closeModal() {
    this.showModal.set(false);
  }

  async saveUser() {
    if (!this.form.name.trim() || !this.form.email.trim()) {
      this.formError = 'Preencha nome e e-mail.';
      return;
    }

    if (!this.editingUser() && this.form.password.trim().length < 8) {
      this.formError = 'Informe uma senha com pelo menos 8 caracteres.';
      return;
    }

    if (this.form.password.trim() && this.form.password.trim().length < 8) {
      this.formError = 'A senha deve ter pelo menos 8 caracteres.';
      return;
    }

    const payload = {
      name: this.form.name.trim(),
      email: this.form.email.trim(),
      role: Number(this.form.role),
      active: Boolean(this.form.active),
      ...(this.form.password.trim() ? { password: this.form.password.trim() } : {}),
    };

    try {
      const editing = this.editingUser();
      if (editing) {
        await this.api.updateUser(editing.id, payload);
      } else {
        await this.api.createUser(payload);
      }
      await this.load();
      this.closeModal();
    } catch (error) {
      this.formError = error instanceof Error ? error.message : 'Nao foi possivel salvar o usuario.';
    }
  }

  async deleteUser(user: User) {
    if (!confirm(`Excluir o usuario "${user.name}"?`)) return;
    try {
      await this.api.deleteUser(user.id);
      await this.load();
    } catch (error) {
      this.formError = error instanceof Error ? error.message : 'Nao foi possivel excluir o usuario.';
    }
  }

  getInitials(name: string) {
    return name.split(' ').slice(0, 2).map(n => n[0]).join('').toUpperCase();
  }
}
