import { Component, OnInit, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { UserGamificationService, UserLeaderboard, RecentActivity } from '../user-gamification.service';
import { GamificationNotificationService } from '../gamification-notification.service';
import { AuthService } from '../auth';
import { NavbarComponent } from '../navbar/navbar.component';

declare const feather: any;

interface LeaderboardEntry {
  rank: number;
  name: string;
  totalPoints: number;
  level: number;
  levelName: string;
  badgesCount: number;
  isCurrentUser: boolean;
}

@Component({
  selector: 'app-classement',
  standalone: true,
  imports: [CommonModule, RouterModule, NavbarComponent],
  templateUrl: './classement.html',
  styleUrls: ['./classement.css']
})
export class ClassementComponent implements OnInit, AfterViewInit {
  leaderboard: LeaderboardEntry[] = [];
  selectedPeriod = 'all';

  // Notifications
  recentActivity: RecentActivity[] = [];
  userPosition: LeaderboardEntry | null = null;
  loading = true;
  error = '';

  periodOptions = [
    { value: 'all', label: 'Tout temps' },
    { value: 'month', label: 'Ce mois' },
    { value: 'week', label: 'Cette semaine' }
  ];

  constructor(
    private gamificationService: UserGamificationService,
    public authService: AuthService,
    private notificationService: GamificationNotificationService,
    private router: Router
  ) { }

  ngOnInit() {
    this.loadLeaderboard();

    // Notifications logic
    this.notificationService.checkForNewAchievements();
  }
  ngAfterViewInit() {
    if (typeof feather !== 'undefined') {
      setTimeout(() => feather.replace(), 100);
    }
  }


  loadLeaderboard() {
    this.gamificationService.getUserLeaderboard().subscribe({
      next: (data) => {
        this.userPosition = data.userPosition;
        this.leaderboard = data.topLeaderboard;
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur chargement classement:', err);
        this.error = 'Erreur lors du chargement du classement';
        this.loading = false;

        // Données de démonstration en cas d'erreur
        this.generateDemoLeaderboard();
      }
    });
  }

  generateDemoLeaderboard() {
    const currentUserEmail = this.authService.getEmail() || 'user@example.com';
    const currentUserName = currentUserEmail.split('@')[0];

    this.leaderboard = [
      {
        rank: 1,
        name: 'Alice Martin',
        totalPoints: 2850,
        level: 8,
        levelName: 'Expert',
        badgesCount: 15,
        isCurrentUser: false
      },
      {
        rank: 2,
        name: 'Thomas Dubois',
        totalPoints: 2640,
        level: 7,
        levelName: 'Avancé',
        badgesCount: 12,
        isCurrentUser: false
      },
      {
        rank: 3,
        name: 'Sophie Laurent',
        totalPoints: 2420,
        level: 7,
        levelName: 'Avancé',
        badgesCount: 11,
        isCurrentUser: false
      },
      {
        rank: 4,
        name: 'Pierre Moreau',
        totalPoints: 2180,
        level: 6,
        levelName: 'Intermédiaire',
        badgesCount: 9,
        isCurrentUser: false
      },
      {
        rank: 5,
        name: 'Marie Leroy',
        totalPoints: 1950,
        level: 6,
        levelName: 'Intermédiaire',
        badgesCount: 8,
        isCurrentUser: false
      },
      {
        rank: 6,
        name: currentUserName,
        totalPoints: 1720,
        level: 5,
        levelName: 'Intermédiaire',
        badgesCount: 7,
        isCurrentUser: true
      },
      {
        rank: 7,
        name: 'Julie Bernard',
        totalPoints: 1580,
        level: 5,
        levelName: 'Intermédiaire',
        badgesCount: 6,
        isCurrentUser: false
      },
      {
        rank: 8,
        name: 'Nicolas Petit',
        totalPoints: 1420,
        level: 4,
        levelName: 'Débutant+',
        badgesCount: 5,
        isCurrentUser: false
      },
      {
        rank: 9,
        name: 'Emma Roux',
        totalPoints: 1280,
        level: 4,
        levelName: 'Débutant+',
        badgesCount: 4,
        isCurrentUser: false
      },
      {
        rank: 10,
        name: 'Lucas Garcia',
        totalPoints: 1150,
        level: 3,
        levelName: 'Débutant',
        badgesCount: 3,
        isCurrentUser: false
      }
    ];

    // Position de l'utilisateur actuel
    this.userPosition = this.leaderboard.find(entry => entry.isCurrentUser) || {
      rank: 6,
      name: currentUserName,
      totalPoints: 1720,
      level: 5,
      levelName: 'Intermédiaire',
      badgesCount: 7,
      isCurrentUser: true
    };
  }

  getRankIcon(rank: number): string {
    switch (rank) {
      case 1: return 'award'; // Badge doré pour le 1er
      case 2: return 'award'; // Badge argenté pour le 2ème  
      case 3: return 'award'; // Badge bronze pour le 3ème
      default: return 'user';
    }
  }

  getRankColor(rank: number): string {
    switch (rank) {
      case 1: return 'from-yellow-400 to-yellow-600'; // Or pour le 1er
      case 2: return 'from-gray-400 to-gray-600';     // Argent pour le 2ème
      case 3: return 'from-amber-600 to-orange-700';  // Bronze pour le 3ème
      default: return 'from-blue-400 to-blue-600';
    }
  }

  getRankBadgeColor(rank: number): string {
    switch (rank) {
      case 1: return 'bg-yellow-100 text-yellow-800';
      case 2: return 'bg-gray-100 text-gray-800';
      case 3: return 'bg-orange-100 text-orange-800';
      default: return 'bg-blue-100 text-blue-800';
    }
  }

  getLevelColor(level: number): string {
    if (level >= 8) return 'text-purple-600';
    if (level >= 6) return 'text-blue-600';
    if (level >= 4) return 'text-green-600';
    return 'text-gray-600';
  }

  getInitials(name: string): string {
    return name.split(' ').map(n => n.charAt(0)).join('').toUpperCase();
  }

  onPeriodChange(period: string) {
    this.selectedPeriod = period;
    // Ici on pourrait recharger les données selon la période
    // Pour la démo, on garde les mêmes données
  }

  logout() {
    this.authService.logout();
  }


  toggleNotifications() {
    // Handled by Navbar
  }
}