import { Component, OnInit, AfterViewInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../auth';
import { UserGamificationService, UserGamificationStats, RecentActivity } from '../user-gamification.service';
import { ParcoursNotificationService, ParcoursNotification } from '../parcours-notification.service';
import { LevelNotificationService, LevelNotification } from '../level-notification.service';
import { BadgeNotificationService, BadgeNotification } from '../badge-notification.service';
import { ChallengeNotificationService, ChallengeNotification } from '../challenge-notification.service';
import { ParcoursAutoRefreshService } from '../parcours-auto-refresh.service';
import { EnrollmentService } from '../enrollment.service';
import { Subscription } from 'rxjs';

declare const feather: any;

@Component({
    selector: 'app-navbar',
    standalone: true,
    imports: [CommonModule, RouterModule],
    templateUrl: './navbar.component.html',
    styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit, AfterViewInit, OnDestroy {
    userInitials = 'ET';
    userStats: UserGamificationStats | null = null;
    enrolledCount = 0;
    userProfileImage = '';
    showNotifications = false;
    recentActivity: RecentActivity[] = [];
    parcoursNotifications: ParcoursNotification[] = [];
    levelNotifications: LevelNotification[] = [];
    badgeNotifications: BadgeNotification[] = [];
    challengeNotifications: ChallengeNotification[] = [];
    unreadNotificationsCount = 0;
    private profileSub: Subscription | null = null;
    private autoRefreshSub: Subscription | null = null;
    private statsUpdateSub: Subscription | null = null;

    constructor(
        public authService: AuthService,
        private router: Router,
        private userStatsService: UserGamificationService,
        private parcoursNotificationService: ParcoursNotificationService,
        private levelNotificationService: LevelNotificationService,
        private badgeNotificationService: BadgeNotificationService,
        private challengeNotificationService: ChallengeNotificationService,
        private parcoursAutoRefreshService: ParcoursAutoRefreshService,
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

            // ✅ RÉACTIVÉ avec fréquence optimisée: Service de mise à jour automatique
            this.initializeAutoRefresh();
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
        if (this.autoRefreshSub) {
            this.autoRefreshSub.unsubscribe();
        }
        if (this.statsUpdateSub) {
            this.statsUpdateSub.unsubscribe();
        }
        
        // Arrêter le service auto-refresh
        this.parcoursAutoRefreshService.stopAutoRefresh();
        
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
            // Charger les activités récentes (pour compatibilité - peut être supprimé plus tard)
            this.userStatsService.getRecentActivity(5).subscribe({
                next: (activities) => {
                    this.recentActivity = activities;
                }
            });

            // Charger les notifications de parcours
            this.parcoursNotificationService.getUnreadNotifications().subscribe({
                next: (notifications) => {
                    this.parcoursNotifications = notifications;
                }
            });

            // Charger les notifications de niveau
            this.levelNotificationService.getUnreadLevelNotifications().subscribe({
                next: (notifications) => {
                    this.levelNotifications = notifications;
                }
            });

            // Charger les notifications de badge
            this.badgeNotificationService.getUnreadBadgeNotifications().subscribe({
                next: (notifications) => {
                    this.badgeNotifications = notifications;
                },
                error: (error) => {
                    console.warn('Service de notifications de badge non disponible:', error);
                    this.badgeNotifications = [];
                }
            });

            // Charger les notifications de défi
            this.challengeNotificationService.getUnreadChallengeNotifications().subscribe({
                next: (notifications) => {
                    this.challengeNotifications = notifications;
                },
                error: (error) => {
                    console.warn('Service de notifications de défi non disponible:', error);
                    this.challengeNotifications = [];
                }
            });

            // Charger le nombre total de notifications non lues
            this.updateUnreadNotificationsCount();
        }
    }

    private updateUnreadNotificationsCount() {
        let totalCount = 0;
        let completedRequests = 0;
        const totalRequests = 4; // parcours, niveau, badge, défi

        const updateTotal = () => {
            completedRequests++;
            if (completedRequests === totalRequests) {
                this.unreadNotificationsCount = totalCount;
            }
        };

        // Compter les notifications de parcours
        this.parcoursNotificationService.getUnreadNotificationsCount().subscribe({
            next: (response) => {
                totalCount += response.count;
                updateTotal();
            },
            error: () => updateTotal()
        });

        // Compter les notifications de niveau
        this.levelNotificationService.getUnreadLevelNotificationsCount().subscribe({
            next: (response) => {
                totalCount += response.count;
                updateTotal();
            },
            error: () => updateTotal()
        });

        // Compter les notifications de badge
        this.badgeNotificationService.getUnreadBadgeNotificationsCount().subscribe({
            next: (response) => {
                totalCount += response.count;
                updateTotal();
            },
            error: (error) => {
                console.warn('Compteur de notifications de badge non disponible:', error);
                updateTotal();
            }
        });

        // Compter les notifications de défi
        this.challengeNotificationService.getUnreadChallengeNotificationsCount().subscribe({
            next: (response) => {
                totalCount += response.count;
                updateTotal();
            },
            error: (error) => {
                console.warn('Compteur de notifications de défi non disponible:', error);
                updateTotal();
            }
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

    toggleNotifications() {
        this.showNotifications = !this.showNotifications;
        if (this.showNotifications) {
            // Recharger les notifications quand on ouvre le panneau
            this.loadNotifications();
            this.refreshIcons();
        }
    }

    markNotificationAsRead(notificationId: number) {
        this.parcoursNotificationService.markNotificationAsRead(notificationId).subscribe({
            next: () => {
                // Retirer la notification de la liste des non lues
                this.parcoursNotifications = this.parcoursNotifications.filter(n => n.id !== notificationId);
                this.updateUnreadNotificationsCount();
            }
        });
    }

    markLevelNotificationAsRead(notificationId: number) {
        this.levelNotificationService.markNotificationAsRead(notificationId).subscribe({
            next: () => {
                // Retirer la notification de la liste des non lues
                this.levelNotifications = this.levelNotifications.filter(n => n.id !== notificationId);
                this.updateUnreadNotificationsCount();
            }
        });
    }

    markBadgeNotificationAsRead(notificationId: number) {
        this.badgeNotificationService.markNotificationAsRead(notificationId).subscribe({
            next: () => {
                // Retirer la notification de la liste des non lues
                this.badgeNotifications = this.badgeNotifications.filter(n => n.id !== notificationId);
                this.updateUnreadNotificationsCount();
            }
        });
    }

    markChallengeNotificationAsRead(notificationId: number) {
        this.challengeNotificationService.markNotificationAsRead(notificationId).subscribe({
            next: () => {
                // Retirer la notification de la liste des non lues
                this.challengeNotifications = this.challengeNotifications.filter(n => n.id !== notificationId);
                this.updateUnreadNotificationsCount();
            }
        });
    }

    markAllNotificationsAsRead() {
        // Marquer toutes les notifications de parcours comme lues
        this.parcoursNotificationService.markAllNotificationsAsRead().subscribe({
            next: () => {
                this.parcoursNotifications = [];
            }
        });

        // Marquer toutes les notifications de niveau comme lues
        this.levelNotificationService.markAllNotificationsAsRead().subscribe({
            next: () => {
                this.levelNotifications = [];
            }
        });

        // Marquer toutes les notifications de badge comme lues
        this.badgeNotificationService.markAllNotificationsAsRead().subscribe({
            next: () => {
                this.badgeNotifications = [];
            }
        });

        // Marquer toutes les notifications de défi comme lues
        this.challengeNotificationService.markAllNotificationsAsRead().subscribe({
            next: () => {
                this.challengeNotifications = [];
                this.unreadNotificationsCount = 0;
            }
        });
    }

    getUserDomaine(): string {
        // D'abord essayer depuis le localStorage
        const domaine = this.authService.getDomaine();
        if (domaine && domaine.trim() !== '') {
            return domaine;
        }
        
        // Ensuite essayer depuis le profil utilisateur
        const profile = this.authService.getCurrentProfile();
        if (profile && profile.role === 'FORMATEUR' && (profile as any).domaineSpecialisation) {
            return (profile as any).domaineSpecialisation;
        }
        
        // Valeur par défaut pour les formateurs
        if (this.authService.isFormateur()) {
            return 'Développement Web';
        }
        
        return 'Expert Digital';
    }

    goToProfile() {
        this.router.navigate(['/profile']);
    }

    logout() {
        this.authService.logout();
    }

    /**
     * Initialise le service de mise à jour automatique
     */
    private initializeAutoRefresh() {
        console.log('🔄 Initialisation du service auto-refresh dans navbar');
        
        // Démarrer le service
        this.parcoursAutoRefreshService.startAutoRefresh();
        
        // S'abonner aux nouvelles notifications
        this.autoRefreshSub = this.parcoursAutoRefreshService.newNotification$.subscribe(notification => {
            if (notification) {
                console.log('🆕 Nouvelle notification reçue dans navbar:', notification.title);
                // Recharger les notifications
                this.loadNotifications();
            }
        });
        
        // S'abonner aux mises à jour des stats
        this.statsUpdateSub = this.parcoursAutoRefreshService.statsUpdated$.subscribe(stats => {
            if (stats) {
                console.log('📊 Stats mises à jour dans navbar');
                this.userStats = stats;
            }
        });
    }
}
