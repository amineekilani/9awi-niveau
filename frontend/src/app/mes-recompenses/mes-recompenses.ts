import { Component, OnInit, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { UserGamificationService, UserBadge } from '../user-gamification.service';
import { AuthService } from '../auth';

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
  badgesCount = 0;

  filterOptions = [
    { value: 'all', label: 'Toutes les récompenses', count: 0 },
    { value: 'earned', label: 'Obtenues', count: 0 },
    { value: 'new', label: 'Nouvelles', count: 0 },
    { value: 'locked', label: 'À débloquer', count: 0 }
  ];

  constructor(
    private gamificationService: UserGamificationService,
    public authService: AuthService
  ) { }

  ngOnInit() {
    this.calculateUserInitials();
    this.loadBadges();
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
        this.badges = badges;
        this.badgesCount = badges.filter(b => b.earnedAt > 0).length;
        this.updateFilterCounts();
        this.applyFilter();
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur chargement badges:', err);
        this.error = 'Erreur lors du chargement des récompenses';
        this.loading = false;

        // Données de démonstration en cas d'erreur
        this.badges = this.generateDemoBadges();
        this.badgesCount = this.badges.filter(b => b.earnedAt > 0).length;
        this.updateFilterCounts();
        this.applyFilter();
      }
    });
  }

  generateDemoBadges(): UserBadge[] {
    return [
      {
        id: 1,
        name: 'Premier Pas',
        description: 'Première connexion à la plateforme',
        iconUrl: 'badge-first-login.png',
        earnedAt: Date.now() - 86400000,
        isNew: false
      },
      {
        id: 2,
        name: 'Lecteur Assidu',
        description: 'Terminer 3 leçons',
        iconUrl: 'badge-reader.png',
        earnedAt: Date.now() - 3600000,
        isNew: true
      },
      {
        id: 3,
        name: 'Quiz Master',
        description: 'Réussir 5 quiz avec 100%',
        iconUrl: 'badge-quiz-master.png',
        earnedAt: 0,
        isNew: false
      },
      {
        id: 4,
        name: 'Explorateur',
        description: 'S\'inscrire à 5 cours différents',
        iconUrl: 'badge-explorer.png',
        earnedAt: Date.now() - 7200000,
        isNew: false
      },
      {
        id: 5,
        name: 'Persévérant',
        description: 'Se connecter 7 jours consécutifs',
        iconUrl: 'badge-persistent.png',
        earnedAt: 0,
        isNew: false
      },
      {
        id: 6,
        name: 'Expert',
        description: 'Atteindre le niveau 5',
        iconUrl: 'badge-expert.png',
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

  getBadgeImageUrl(iconUrl: string): string {
    return `http://localhost:8080/images/badges/${iconUrl}`;
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
}