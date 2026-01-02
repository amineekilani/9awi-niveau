import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { GamificationService, LeaderboardResponse } from '../gamification.service';

@Component({
  selector: 'app-leaderboard-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './leaderboard-management.html',
  styleUrls: ['./leaderboard-management.css']
})
export class LeaderboardManagementComponent implements OnInit {
  topLeaderboard: LeaderboardResponse | null = null;
  fullLeaderboard: LeaderboardResponse | null = null;
  loading = false;
  error = '';
  
  // Vue active
  activeView: 'podium' | 'full' = 'podium';
  
  // Pagination pour la vue complète
  currentPage = 0;
  pageSize = 20;
  
  // Limite pour le podium
  topLimit = 10;

  constructor(private gamificationService: GamificationService) {}

  ngOnInit() {
    this.loadTopLeaderboard();
  }

  setActiveView(view: 'podium' | 'full') {
    this.activeView = view;
    if (view === 'podium' && !this.topLeaderboard) {
      this.loadTopLeaderboard();
    } else if (view === 'full' && !this.fullLeaderboard) {
      this.loadFullLeaderboard();
    }
  }

  loadTopLeaderboard() {
    this.loading = true;
    this.error = '';

    this.gamificationService.getTopLeaderboard(this.topLimit).subscribe({
      next: (response) => {
        this.topLeaderboard = response;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading top leaderboard:', error);
        this.error = 'Erreur lors du chargement du classement';
        this.loading = false;
      }
    });
  }

  loadFullLeaderboard() {
    this.loading = true;
    this.error = '';

    this.gamificationService.getLeaderboard(this.currentPage, this.pageSize).subscribe({
      next: (response) => {
        this.fullLeaderboard = response;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading full leaderboard:', error);
        this.error = 'Erreur lors du chargement du classement complet';
        this.loading = false;
      }
    });
  }

  exportLeaderboard() {
    this.loading = true;
    this.error = '';

    this.gamificationService.exportLeaderboard().subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = 'classement-gamification.csv';
        link.click();
        window.URL.revokeObjectURL(url);
        this.loading = false;
      },
      error: (error) => {
        console.error('Error exporting leaderboard:', error);
        this.error = 'Erreur lors de l\'export du classement';
        this.loading = false;
      }
    });
  }

  private generateCSV(entries: any[]): string {
    const headers = ['Rang', 'Prénom', 'Nom', 'Email', 'XP Total', 'Niveau', 'Nom du Niveau', 'Badges'];
    const csvRows = [headers.join(',')];

    entries.forEach(entry => {
      const row = [
        entry.rank,
        `"${entry.firstName}"`,
        `"${entry.lastName}"`,
        `"${entry.email}"`,
        entry.totalXP,
        entry.currentLevel,
        `"${entry.levelName}"`,
        entry.badgesCount
      ];
      csvRows.push(row.join(','));
    });

    return csvRows.join('\n');
  }

  private downloadCSV(content: string, filename: string) {
    const blob = new Blob([content], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    
    if (link.download !== undefined) {
      const url = URL.createObjectURL(blob);
      link.setAttribute('href', url);
      link.setAttribute('download', filename);
      link.style.visibility = 'hidden';
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    }
  }

  changePage(page: number) {
    this.currentPage = page;
    this.loadFullLeaderboard();
  }

  refreshData() {
    if (this.activeView === 'podium') {
      this.loadTopLeaderboard();
    } else {
      this.loadFullLeaderboard();
    }
  }

  getRankClass(rank: number): string {
    if (rank === 1) return 'gold';
    if (rank === 2) return 'silver';
    if (rank === 3) return 'bronze';
    return '';
  }

  getRankIcon(rank: number): string {
    if (rank === 1) return '🥇';
    if (rank === 2) return '🥈';
    if (rank === 3) return '🥉';
    return `#${rank}`;
  }

  formatXP(xp: number): string {
    if (xp >= 1000000) {
      return (xp / 1000000).toFixed(1) + 'M';
    } else if (xp >= 1000) {
      return (xp / 1000).toFixed(1) + 'K';
    }
    return xp.toString();
  }

  getLevelColor(level: number): string {
    if (level >= 8) return '#ff6b35'; // Orange pour les niveaux élevés
    if (level >= 5) return '#f7931e'; // Orange moyen
    if (level >= 3) return '#4caf50'; // Vert
    return '#2196f3'; // Bleu pour les débutants
  }
}