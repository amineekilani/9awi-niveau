import { Component, OnInit, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { UserGamificationService, UserChallenge } from '../user-gamification.service';
import { AuthService } from '../auth';

declare const feather: any;

@Component({
  selector: 'app-mes-defis',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './mes-defis.html',
  styleUrls: ['./mes-defis.css']
})
export class MesDefisComponent implements OnInit, AfterViewInit {
  challenges: UserChallenge[] = [];
  filteredChallenges: UserChallenge[] = [];
  loading = true;
  error = '';
  selectedFilter = 'active';
  userInitials = 'ET';

  filterOptions = [
    { value: 'active', label: 'Défis actifs', count: 0 },
    { value: 'completed', label: 'Terminés', count: 0 },
    { value: 'all', label: 'Tous les défis', count: 0 }
  ];

  constructor(
    private gamificationService: UserGamificationService,
    public authService: AuthService
  ) {}

  ngOnInit() {
    this.calculateUserInitials();
    this.loadChallenges();
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

  loadChallenges() {
    this.gamificationService.getUserChallenges().subscribe({
      next: (challenges) => {
        this.challenges = challenges;
        this.updateFilterCounts();
        this.applyFilter();
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur chargement défis:', err);
        this.error = 'Erreur lors du chargement des défis';
        this.loading = false;
        
        // Données de démonstration en cas d'erreur
        this.challenges = this.generateDemoChallenges();
        this.updateFilterCounts();
        this.applyFilter();
      }
    });
  }

  generateDemoChallenges(): UserChallenge[] {
    const now = Date.now();
    return [
      {
        id: 1,
        name: 'Marathon d\'apprentissage',
        description: 'Terminer 10 leçons cette semaine',
        challengeType: 'WEEKLY',
        targetValue: 10,
        currentProgress: 7,
        progressPercent: 70,
        xpReward: 200,
        isCompleted: false,
        joinedAt: now - 86400000 * 2,
        endDate: now + 86400000 * 5,
        timeRemaining: '5 jours restants',
        isActive: true
      },
      {
        id: 2,
        name: 'Quiz Champion',
        description: 'Réussir 5 quiz avec une note parfaite',
        challengeType: 'ACHIEVEMENT',
        targetValue: 5,
        currentProgress: 3,
        progressPercent: 60,
        xpReward: 150,
        isCompleted: false,
        joinedAt: now - 86400000 * 7,
        isActive: true
      },
      {
        id: 3,
        name: 'Explorateur de cours',
        description: 'S\'inscrire à 3 nouveaux cours',
        challengeType: 'MONTHLY',
        targetValue: 3,
        currentProgress: 3,
        progressPercent: 100,
        xpReward: 300,
        isCompleted: true,
        completedAt: now - 86400000,
        joinedAt: now - 86400000 * 15,
        isActive: false
      },
      {
        id: 4,
        name: 'Régularité',
        description: 'Se connecter 7 jours consécutifs',
        challengeType: 'DAILY',
        targetValue: 7,
        currentProgress: 4,
        progressPercent: 57,
        xpReward: 100,
        isCompleted: false,
        joinedAt: now - 86400000 * 4,
        isActive: true
      },
      {
        id: 5,
        name: 'Maître des modules',
        description: 'Terminer tous les modules d\'un cours',
        challengeType: 'ACHIEVEMENT',
        targetValue: 1,
        currentProgress: 1,
        progressPercent: 100,
        xpReward: 250,
        isCompleted: true,
        completedAt: now - 86400000 * 3,
        joinedAt: now - 86400000 * 10,
        isActive: false
      }
    ];
  }

  updateFilterCounts() {
    const active = this.challenges.filter(c => c.isActive && !c.isCompleted);
    const completed = this.challenges.filter(c => c.isCompleted);

    this.filterOptions[0].count = active.length;
    this.filterOptions[1].count = completed.length;
    this.filterOptions[2].count = this.challenges.length;
  }

  applyFilter() {
    switch (this.selectedFilter) {
      case 'active':
        this.filteredChallenges = this.challenges.filter(c => c.isActive && !c.isCompleted);
        break;
      case 'completed':
        this.filteredChallenges = this.challenges.filter(c => c.isCompleted);
        break;
      default:
        this.filteredChallenges = [...this.challenges];
    }
  }

  onFilterChange(filter: string) {
    this.selectedFilter = filter;
    this.applyFilter();
  }

  getChallengeTypeIcon(type: string): string {
    switch (type) {
      case 'DAILY': return 'sun';
      case 'WEEKLY': return 'calendar';
      case 'MONTHLY': return 'calendar';
      case 'ACHIEVEMENT': return 'trophy';
      default: return 'target';
    }
  }

  getChallengeTypeColor(type: string): string {
    switch (type) {
      case 'DAILY': return 'from-yellow-400 to-orange-500';
      case 'WEEKLY': return 'from-blue-400 to-blue-600';
      case 'MONTHLY': return 'from-purple-400 to-purple-600';
      case 'ACHIEVEMENT': return 'from-green-400 to-green-600';
      default: return 'from-gray-400 to-gray-600';
    }
  }

  formatDate(timestamp: number): string {
    return new Date(timestamp).toLocaleDateString('fr-FR', {
      day: 'numeric',
      month: 'long',
      year: 'numeric'
    });
  }

  logout() {
    this.authService.logout();
  }
}