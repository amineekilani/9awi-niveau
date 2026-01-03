import { Component, OnInit, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { UserGamificationService, UserBadge, RecentActivity } from '../user-gamification.service';
import { AuthService } from '../auth';
import { GamificationNotificationService } from '../gamification-notification.service';

declare const feather: any;

@Component({
  selector: 'app-mes-recompenses',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './mes-recompenses.html',
  styleUrls: ['./mes-recompenses.css']
})
export class MesRecompensesComponent implements OnInit, AfterViewInit {
  badges: UserBadge[] = [];
  filteredBadges: UserBadge[] = [];
  loading = true;
  error = '';
  selectedFilter = 'all';
  userInitials = 'ET';
  userProfileImage = '';
  badgesCount = 0;

  // Notifications
  showNotifications = false;
  recentActivity: RecentActivity[] = [];

  filterOptions = [
    { value: 'all', label: 'Toutes les récompenses', count: 0 },
    { value: 'earned', label: 'Obtenues', count: 0 },
    { value: 'new', label: 'Nouvelles', count: 0 },
    { value: 'locked', label: 'À débloquer', count: 0 }
  ];

  constructor(
    private gamificationService: UserGamificationService,
    public authService: AuthService,
    private notificationService: GamificationNotificationService,
    private router: Router
  ) { }

  ngOnInit() {
    this.authService.userProfile$.subscribe(profile => {
      if (profile) {
        this.userProfileImage = profile.profileImage || '';
        const firstName = profile.firstName || '';
        const lastName = profile.lastName || '';
        if (firstName && lastName) {
          this.userInitials = (firstName.charAt(0) + lastName.charAt(0)).toUpperCase();
        } else if (profile.email) {
          const parts = profile.email.split('@')[0].split('.');
          this.userInitials = parts.map(p => p.charAt(0).toUpperCase()).join('').substring(0, 2);
        }
      }
    });
    // Charger le profil si pas encore fait (sécurité)
    if (this.authService.getToken() && !this.userProfileImage) {
      this.authService.loadUserProfile();
    }

    this.loadBadges();

    // Notifications logic
    this.notificationService.checkForNewAchievements();
    this.loadNotifications();

    document.addEventListener('click', (event: any) => {
      const target = event.target as HTMLElement;
      if (!target.closest('.notification-container')) {
        this.showNotifications = false;
      }
    });
  }
  ngAfterViewInit() {
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

  loadBadges() {
    this.gamificationService.getUserBadges().subscribe({
      next: (badges) => {
        console.log('Badges loaded from API:', badges);
        this.badges = badges;
        this.badgesCount = badges.filter(b => b.earnedAt > 0).length;
        this.updateFilterCounts();
        this.applyFilter();
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur chargement badges:', err);
        this.error = 'Erreur lors du chargement des récompenses. Vérifiez que le backend est démarré.';
        this.loading = false;
      }
    });
  }

  generateDemoBadges(): UserBadge[] {
    return [
      {
        id: 1,
        name: 'Premier Cours',
        description: 'Terminer votre premier cours',
        iconUrl: 'first-course.svg',
        earnedAt: Date.now() - 86400000,
        isNew: false
      },
      {
        id: 2,
        name: 'Score Parfait',
        description: 'Obtenir 100% à un quiz',
        iconUrl: 'perfect-score.svg',
        earnedAt: Date.now() - 3600000,
        isNew: true
      },
      {
        id: 3,
        name: 'Maître des Quiz',
        description: 'Réussir 10 quiz avec une note supérieure à 80%',
        iconUrl: 'quiz-master.svg',
        earnedAt: 0,
        isNew: false
      },
      {
        id: 4,
        name: 'Série Gagnante',
        description: 'Se connecter 7 jours consécutifs',
        iconUrl: 'streak-master.svg',
        earnedAt: 0,
        isNew: false
      },
      {
        id: 5,
        name: 'Explorateur',
        description: 'S\'inscrire à 5 cours différents',
        iconUrl: 'default-badge.svg',
        earnedAt: Date.now() - 7200000,
        isNew: false
      },
      {
        id: 6,
        name: 'Persévérant',
        description: 'Terminer 3 cours complets',
        iconUrl: 'default-badge.svg',
        earnedAt: 0,
        isNew: false
      },
      {
        id: 7,
        name: 'Expert',
        description: 'Atteindre le niveau 10',
        iconUrl: 'default-badge.svg',
        earnedAt: 0,
        isNew: false
      },
      {
        id: 8,
        name: 'Collectionneur',
        description: 'Obtenir 5 badges différents',
        iconUrl: 'default-badge.svg',
        earnedAt: 0,
        isNew: false
      }
    ];
  }

  updateFilterCounts() {
    const earned = this.badges.filter(b => b.earnedAt > 0);
    const newBadges = this.badges.filter(b => b.isNew && b.earnedAt > 0);
    const locked = this.badges.filter(b => b.earnedAt === 0);

    this.filterOptions[0].count = this.badges.length;
    this.filterOptions[1].count = earned.length;
    this.filterOptions[2].count = newBadges.length;
    this.filterOptions[3].count = locked.length;
  }

  applyFilter() {
    switch (this.selectedFilter) {
      case 'earned':
        this.filteredBadges = this.badges.filter(b => b.earnedAt > 0);
        break;
      case 'new':
        this.filteredBadges = this.badges.filter(b => b.isNew && b.earnedAt > 0);
        break;
      case 'locked':
        this.filteredBadges = this.badges.filter(b => b.earnedAt === 0);
        break;
      default:
        this.filteredBadges = [...this.badges];
    }
  }

  onFilterChange(filter: string) {
    this.selectedFilter = filter;
    this.applyFilter();
  }

  getBadgeImageUrl(badge: UserBadge): string {
    // Si l'URL est fournie et valide
    if (badge.iconUrl && (badge.iconUrl.startsWith('http') || badge.iconUrl.startsWith('/'))) {
      return badge.iconUrl;
    }

    // Si l'URL est juste un nom de fichier, ajoutez le préfixe
    if (badge.iconUrl) {
      return `/badges/${badge.iconUrl}`;
    }

    // Sinon, essayez de déduire l'icône à partir du type de critère
    if (badge.criteriaType) {
      return this.getDefaultBadgeIcon(badge.criteriaType);
    }

    // Fallback final
    return '/badges/default-badge.svg';
  }

  getDefaultBadgeIcon(criteriaType: string): string {
    // Mapping vers les fichiers existants uniquement:
    // streak-master.svg, quiz-master.svg, perfect-score.svg, first-course.svg, default-badge.svg
    const iconMap: { [key: string]: string } = {
      'FIRST_COURSE': '/badges/first-course.svg',
      'COURS_COMPLETED': '/badges/first-course.svg',
      'QUIZ_PASSED': '/badges/quiz-master.svg',
      'FIRST_QUIZ': '/badges/quiz-master.svg',
      'PERFECT_SCORE': '/badges/perfect-score.svg',
      'DAILY_LOGIN': '/badges/streak-master.svg', // Corrigé
      'EARN_XP': '/badges/default-badge.svg',     // Fallback
      'COMPLETE_MODULE': '/badges/first-course.svg', // Réutilisation
      'EARN_BADGES': '/badges/default-badge.svg'  // Fallback
    };
    return iconMap[criteriaType] || '/badges/default-badge.svg';
  }

  onImageError(event: any) {
    event.target.src = '/badges/default-badge.svg';
  }

  formatDate(timestamp: number): string {
    if (timestamp === 0) return '';
    return new Date(timestamp).toLocaleDateString('fr-FR', {
      day: 'numeric',
      month: 'long',
      year: 'numeric'
    });
  }

  logout() {
    this.authService.logout();
  }

  goToProfile() {
    this.router.navigate(['/profile']);
  }

  // --- Notifications Logic ---

  loadNotifications() {
    this.gamificationService.getRecentActivity(5).subscribe({
      next: (activities) => {
        this.recentActivity = activities;
      }
    });
  }

  toggleNotifications() {
    this.showNotifications = !this.showNotifications;
    if (this.showNotifications) {
      setTimeout(() => feather.replace(), 100);
    }
  }
}