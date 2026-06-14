import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { SlicePipe } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { MockDataService } from '../../../core/services/mock-data.service';
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
  private dataService = inject(MockDataService);

  users: User[] = [];
  roles = ROLES;
  roleKeys = Object.keys(ROLES).map(Number);

  showModal = signal(false);
  editingUser = signal<User | null>(null);

  form = { name: '', email: '', role: 3, active: true };
  formError = '';

  ngOnInit() { this.load(); }

  load() { this.users = this.dataService.getUsers(); }

  openCreate() {
    this.editingUser.set(null);
    this.form = { name: '', email: '', role: 3, active: true };
    this.formError = '';
    this.showModal.set(true);
  }

  openEdit(user: User) {
    this.editingUser.set(user);
    this.form = { name: user.name, email: user.email, role: user.role, active: user.active };
    this.formError = '';
    this.showModal.set(true);
  }

  closeModal() { this.showModal.set(false); }

  saveUser() {
    if (!this.form.name.trim() || !this.form.email.trim()) {
      this.formError = 'Preencha nome e e-mail.';
      return;
    }
    const editing = this.editingUser();
    if (editing) {
      this.dataService.updateUser(editing.id, { name: this.form.name, email: this.form.email, role: this.form.role, active: this.form.active });
    } else {
      this.dataService.createUser({ name: this.form.name, email: this.form.email, role: this.form.role, active: this.form.active });
    }
    this.load();
    this.closeModal();
  }

  deleteUser(user: User) {
    if (!confirm(`Excluir o usuário "${user.name}"?`)) return;
    this.dataService.deleteUser(user.id);
    this.load();
  }

  getInitials(name: string) {
    return name.split(' ').slice(0, 2).map(n => n[0]).join('').toUpperCase();
  }
}
