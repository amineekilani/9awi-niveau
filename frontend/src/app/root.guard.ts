import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from './auth';

export const rootGuard: CanActivateFn = () => {
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
    } else {
        router.navigate(['/login']);
        return false;
    }
};
