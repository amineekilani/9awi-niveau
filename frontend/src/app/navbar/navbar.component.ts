import { Component, OnInit, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../auth';
import { UserGamificationService, UserGamificationStats } from '../user-gamification.service';
import { EnrollmentService } from '../enrollment.service';

declare const feather: any;

@Component({
    selector: 'app-navbar',
    standalone: true,
    imports: [CommonModule, RouterModule],
    templateUrl: './navbar.component.html'
})
export class NavbarComponent implements OnInit, AfterViewInit {
    userInitials = 'ET';
    userStats: UserGamificationStats | null = null;
    enrolledCount = 0;

    constructor(
        public authService: AuthService,
        private router: Router,
        private userGamificationService: UserGamificationService,
        private enrollmentService: EnrollmentService
    ) { }

    ngOnInit(): void {
        if (this.authService.getToken()) {
            this.calculateUserInitials();
            this.loadUserStats();
            this.loadEnrolledCount();
        }
    }

    ngAfterViewInit(): void {
        if (typeof feather !== 'undefined') {
            setTimeout(() => feather.replace(), 100);
        }
    }

    calculateUserInitials() {
        const email = this.authService.getEmail();
        if (email) {
            const parts = email.split('@')[0].split('.');
            this.userInitials = parts.map(p => p.charAt(0).toUpperCase()).join('').substring(0, 2);
        }
    }

    loadUserStats() {
        this.userGamificationService.getUserStats().subscribe({
            next: (stats) => {
                this.userStats = stats;
            },
            error: (err) => console.error('Erreur stats navbar:', err)
        });
    }

    loadEnrolledCount() {
        this.enrollmentService.getUserEnrollments().subscribe({
            next: (enrollments) => {
                this.enrolledCount = enrollments.length;
            },
            error: (err) => console.error('Erreur enrollments navbar:', err)
        });
    }

    goToProfile() {
        this.router.navigate(['/profile']);
    }

    logout() {
        this.authService.logout();
    }
}
