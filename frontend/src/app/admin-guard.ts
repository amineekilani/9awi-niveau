import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from './auth';

export const adminGuard = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.getRole() === 'ADMIN') {
    return true;
  } else {
    router.navigate(['/home']);
    return false;
  }
};