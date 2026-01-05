import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from './auth';

export const loginGuard: CanActivateFn = () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (authService.getToken()) {
        if (authService.isAdmin()) {
            router.navigate(['/admin']);
        } else if (authService.isFormateur()) {
            router.navigate(['/formateur-dashboard']);
        } else {
            router.navigate(['/home']);
        }
        return false;
    }
    return true;
};
