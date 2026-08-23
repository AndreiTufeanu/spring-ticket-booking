import { Routes } from '@angular/router';
import { AdminEvents } from './components/admin-events/admin-events';
import { Login } from './components/login/login';
import { authGuard } from './guards/auth.guard';
import { adminGuard } from './guards/admin.guard';
import { UserDashboard } from './components/user-dashboard/user-dashboard';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', component: Login },
  { path: 'admin', component: AdminEvents, canActivate: [authGuard, adminGuard] },
  { path: 'user', component: UserDashboard, canActivate: [authGuard] },
  { path: '**', redirectTo: '/login' }
];
