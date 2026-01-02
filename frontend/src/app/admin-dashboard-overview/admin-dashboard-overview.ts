import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AdminService, UserStatsResponse } from '../admin.service';
import { GamificationService, GamificationStatsResponse } from '../gamification.service';
import { AuthService } from '../auth';

@Component({
  selector: 'app-admin-dashboard-overview',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './admin-dashboard-overview.html',
  styleUrls: ['./admin-dashboard-overview.css']
})
export class AdminDashboardOverviewComponent implements OnInit {
  userStats: UserStatsResponse | null = null;
  gamificationStats: GamificationStatsResponse | null = null;
  loading = false;
  error = '';

  constructor(
    private adminService: AdminService,
    private gamificationService: GamificationService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    // Vérifier l'authentification avant de charger
    if (!this.authService.isAdmin()) {
      this.error = 'Accès non autorisé';
      return;
    }
    
    this.loadStats();
  }

  loadStats() {
    this.loading = true;
    this.error = '';

    // Load user stats first
    this.adminService.getUserStats().subscribe({
      next: (stats) => {
        this.userStats = stats;
        this.loadGamificationStats();
      },
      error: (error) => {
        console.error('Error loading user stats:', error);
        this.error = 'Erreur lors du chargement des statistiques utilisateurs';
        this.loading = false;
        // Try to load gamification stats anyway
        this.loadGamificationStats();
      }
    });
  }

  private loadGamificationStats() {
    this.gamificationService.getGamificationStats().subscribe({
      next: (stats) => {
        this.gamificationStats = stats;
        if (!this.error) {
          this.loading = false;
        }
      },
      error: (error) => {
        console.error('Error loading gamification stats:', error);
        // Don't show error for gamification stats if user stats loaded successfully
        if (!this.userStats) {
          this.error = 'Erreur lors du chargement des statistiques';
        }
        this.loading = false;
      }
    });
  }

  formatNumber(num: number): string {
    if (num >= 1000000) {
      return (num / 1000000).toFixed(1) + 'M';
    } else if (num >= 1000) {
      return (num / 1000).toFixed(1) + 'K';
    }
    return num.toString();
  }
}