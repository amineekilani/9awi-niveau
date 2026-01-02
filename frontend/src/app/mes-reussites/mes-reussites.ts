import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../auth';

declare const feather: any;

interface UserStats {
  totalPoints: number;
  currentLevel: number;
  levelName: string;
  levelDescription: string;
  pointsToNextLevel: number;
  nextLevelPoints: number;
  progressPercent: number;
  badgesCount: number;
  completedChallenges: number;
  leaderboardPosition: number;
  recentActivities: RecentActivity[];
  recentBadges: UserBadge[];
}

interface RecentActivity {
  type: string;
  description: string;
  points: number;
  timeAgo: string;
  icon: string;
}

interface UserBadge {
  id: number;
  name: string;
  description: string;
  iconUrl: string;
  earnedAt: number;
  isNew: boolean;
}

interface UserChallenge {
  id: number;
  name: string;
  description: string;
  currentProgress: number;
  targetValue: number;
  progressPercent: number;
  xpReward: number;
  isCompleted: boolean;
  timeRemaining?: string;
}

@Component({
  selector: 'app-mes-reussites',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './mes-reussites.html',
  styleUrls: ['./mes-reussites.css']
})
export class MesReussitesComponent implements OnInit {
  activeTab = 'overview';
  loading = true;
  userStats: UserStats | null = null;
  allBadges: UserBadge[] = [];
  filteredBadges: UserBadge[] = [];
  badgeFilter = 'all';
  activeChallenges: UserChallenge[] = [];
  completedChallenges: UserChallenge[] = [];
  topLeaderboard: any[] = [];

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private authService: AuthService
  ) {}

  ngOnInit() {
    // Récupérer l'onglet depuis l'URL fragment
    this.route.fragment.subscribe(fragment => {
      if (fragment) {
        this.activeTab = fragment;
      }
    });

    this.loadUserData();
  }

  loadUserData() {
    // Pour l'instant, utiliser des données simulées
    // TODO: Remplacer par des appels API réels
    setTimeout(() => {
      this.userStats = {
        totalPoints: 120,
        currentLevel: 3,
        levelName: 'Expert',
        levelDescription: 'Vous maîtrisez les bases et progressez rapidement',
        pointsToNextLevel: 80,
        nextLevelPoints: 200,
        progressPercent: 60,
        badgesCount: 8,
        completedChallenges: 3,
        leaderboardPosition: 15,
        recentActivities: [
          { type: 'quiz', description: 'Quiz "JavaScript" réussi', points: 15, timeAgo: 'il y a 2h', icon: '✓' },
          { type: 'badge', description: 'Récompense "Expert Quiz" obtenue', points: 0, timeAgo: 'il y a 1j', icon: '🏆' },
          { type: 'course', description: 'Cours "React" terminé', points: 50, timeAgo: 'il y a 3j', icon: '📚' }
        ],
        recentBadges: [
          { id: 1, name: 'Expert Quiz', description: 'Réussissez 10 quiz', iconUrl: '/icons/quiz.svg', earnedAt: Date.now() - 86400000, isNew: true },
          { id: 2, name: 'Premier Cours', description: 'Terminez votre premier cours', iconUrl: '/icons/course.svg', earnedAt: Date.now() - 172800000, isNew: false }
        ]
      };

      this.allBadges = [
        { id: 1, name: 'Expert Quiz', description: 'Réussissez 10 quiz', iconUrl: '/icons/quiz.svg', earnedAt: Date.now() - 86400000, isNew: true },
        { id: 2, name: 'Premier Cours', description: 'Terminez votre premier cours', iconUrl: '/icons/course.svg', earnedAt: Date.now() - 172800000, isNew: false },
        { id: 3, name: 'Score Parfait', description: 'Obtenez 100% à un quiz', iconUrl: '/icons/perfect.svg', earnedAt: Date.now() - 259200000, isNew: false },
        { id: 4, name: 'Assidu', description: 'Connectez-vous 7 jours consécutifs', iconUrl: '/icons/streak.svg', earnedAt: Date.now() - 345600000, isNew: false }
      ];

      this.activeChallenges = [
        { id: 1, name: 'Défi Hebdomadaire', description: 'Terminez 3 cours cette semaine', currentProgress: 2, targetValue: 3, progressPercent: 66.7, xpReward: 100, isCompleted: false, timeRemaining: '3 jours' },
        { id: 2, name: 'Expert Quiz', description: 'Réussissez 5 quiz avec plus de 80%', currentProgress: 3, targetValue: 5, progressPercent: 60, xpReward: 75, isCompleted: false }
      ];

      this.completedChallenges = [
        { id: 3, name: 'Premier Pas', description: 'Terminez votre premier cours', currentProgress: 1, targetValue: 1, progressPercent: 100, xpReward: 50, isCompleted: true }
      ];

      this.topLeaderboard = [
        { rank: 1, name: 'Alice Martin', totalPoints: 450, level: 5, levelName: 'Maître', badgesCount: 15, isCurrentUser: false },
        { rank: 2, name: 'Bob Dupont', totalPoints: 380, level: 4, levelName: 'Avancé', badgesCount: 12, isCurrentUser: false },
        { rank: 15, name: 'Vous', totalPoints: 120, level: 3, levelName: 'Expert', badgesCount: 8, isCurrentUser: true }
      ];

      this.filterBadges();
      this.loading = false;
    }, 1000);
  }

  setActiveTab(tab: string) {
    this.activeTab = tab;
    // Mettre à jour l'URL
    this.router.navigate([], { fragment: tab, relativeTo: this.route });
  }

  setBadgeFilter(filter: string) {
    this.badgeFilter = filter;
    this.filterBadges();
  }

  filterBadges() {
    switch (this.badgeFilter) {
      case 'recent':
        const weekAgo = Date.now() - (7 * 24 * 60 * 60 * 1000);
        this.filteredBadges = this.allBadges.filter(badge => badge.earnedAt > weekAgo);
        break;
      case 'quiz':
        this.filteredBadges = this.allBadges.filter(badge => 
          badge.name.toLowerCase().includes('quiz') || badge.name.toLowerCase().includes('score'));
        break;
      case 'cours':
        this.filteredBadges = this.allBadges.filter(badge => 
          badge.name.toLowerCase().includes('cours') || badge.name.toLowerCase().includes('premier'));
        break;
      default:
        this.filteredBadges = [...this.allBadges];
    }
  }

  goBack() {
    this.router.navigate(['/home']);
  }

  formatDate(timestamp: number): string {
    return new Date(timestamp).toLocaleDateString('fr-FR');
  }

  getRankClass(rank: number): string {
    if (rank === 1) return 'rank-gold';
    if (rank === 2) return 'rank-silver';
    if (rank === 3) return 'rank-bronze';
    return 'rank-default';
  }

  ngAfterViewInit(): void {
    if (typeof feather !== 'undefined') {
      feather.replace();
    }
  }
}