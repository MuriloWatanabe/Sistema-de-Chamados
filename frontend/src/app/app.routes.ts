import { Routes } from '@angular/router';
import { adminGuard } from './core/guards/admin.guard';
import { authGuard } from './core/guards/auth.guard';
import { MainLayoutComponent } from './layouts/main-layout/main-layout.component';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./modules/auth/login/login.component').then(m => m.LoginComponent),
  },
  {
    path: '',
    component: MainLayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      {
        path: 'dashboard',
        loadComponent: () => import('./modules/dashboard/dashboard.component').then(m => m.DashboardComponent),
      },
      {
        path: 'tickets',
        loadComponent: () => import('./modules/tickets/ticket-list/ticket-list.component').then(m => m.TicketListComponent),
      },
      {
        path: 'tickets/novo',
        loadComponent: () => import('./modules/tickets/ticket-form/ticket-form.component').then(m => m.TicketFormComponent),
      },
      {
        path: 'tickets/:id',
        loadComponent: () => import('./modules/tickets/ticket-detail/ticket-detail.component').then(m => m.TicketDetailComponent),
      },
      {
        path: 'usuarios',
        loadComponent: () => import('./modules/users/user-list/user-list.component').then(m => m.UserListComponent),
      },
      {
        path: 'logs',
        canActivate: [adminGuard],
        loadComponent: () => import('./modules/logs/logs.component').then(m => m.LogsComponent),
      },
    ],
  },
  { path: '**', redirectTo: '/dashboard' },
];
