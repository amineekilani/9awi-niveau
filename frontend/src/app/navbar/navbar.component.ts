import { Component, OnInit, AfterViewInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../auth';
import { UserGamificationService, UserGamificationStats, RecentActivity } from '../user-gamification.service';
import { EnrollmentService } from '../enrollment.service';
import { Subscription } from 'rxjs';

declare const feather: any;

@Component({
    selector: 'app-navbar',
    standalone: true,
    imports: [CommonModule, RouterModule],
    templateUrl: './navbar.component.html'
})
export class NavbarComponent implements OnInit, AfterViewInit, OnDestroy {
    userInitials = 'ET';
    userStats: UserGamificationStats | null = null;
    enrolledCount = 0;
    userProfileImage = '';
    showNotifications = false;
    recentActivity: RecentActivity[] = [];
    private profileSub: Subscription | null = null;

    constructor(
        public authService: AuthService,
        private router: Router,
        private userStatsService: UserGamificationService,
        private enrollmentService: EnrollmentService
    ) { }

    ngOnInit(): void {
        if (this.authService.getToken()) {
            // Profile subscription
            this.profileSub = this.authService.userProfile$.subscribe(profile => {
                if (profile) {
                    this.userProfileImage = profile.profileImage || '';

                    if (profile.firstName && profile.lastName) {
                        this.userInitials = (profile.firstName.charAt(0) + profile.lastName.charAt(0)).toUpperCase();
                    } else if (profile.email) {
                        const parts = profile.email.split('@')[0].split('.');
                        this.userInitials = parts.map(p => p.charAt(0).toUpperCase()).join('').substring(0, 2);
                    }
                }
            });

            // Load data
            this.loadUserStats();
            this.loadNotifications();
            this.loadEnrolledCount();

            // Always trigger a load to ensure data is fresh
            this.authService.loadUserProfile();
        }

        // Close notifications on click outside
        document.addEventListener('click', this.onDocumentClick.bind(this));
    }

    ngAfterViewInit(): void {
        this.refreshIcons();
    }

    ngOnDestroy(): void {
        if (this.profileSub) {
            this.profileSub.unsubscribe();
        }
        document.removeEventListener('click', this.onDocumentClick.bind(this));
    }

    private refreshIcons() {
        if (typeof feather !== 'undefined') {
            setTimeout(() => feather.replace(), 100);
        }
    }

    private onDocumentClick(event: any) {
        const target = event.target as HTMLElement;
        if (!target.closest('.notification-container')) {
            this.showNotifications = false;
        }
    }

    loadUserStats() {
        this.userStatsService.getUserStats().subscribe({
            next: (stats) => {
                this.userStats = stats;
            },
            error: (err) => console.error('Erreur stats navbar:', err)
        });
    }

    loadNotifications() {
        if (!this.authService.isFormateur()) {
            this.userStatsService.getRecentActivity(5).subscribe({
                next: (activities) => {
                    this.recentActivity = activities;
                }
            });
        }
    }

    loadEnrolledCount() {
        this.enrollmentService.getUserEnrollments().subscribe({
            next: (enrollments) => {
                this.enrolledCount = enrollments.length;
            },
            error: (err) => console.error('Erreur enrollments navbar:', err)
        });
    }

    toggleNotifications() {
        this.showNotifications = !this.showNotifications;
        if (this.showNotifications) {
            this.refreshIcons();
        }
    }

    goToProfile() {
        this.router.navigate(['/profile']);
    }

    logout() {
        this.authService.logout();
    }
}
