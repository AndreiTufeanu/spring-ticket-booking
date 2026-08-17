import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { ROLES } from '../constants/role';

export const adminGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.getRole() === ROLES.ADMIN) return true;

  router.navigate(['/user']);
  return false;
};