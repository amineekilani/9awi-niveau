import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService, UserStatsResponse } from '../admin.service';
import { GamificationService, GamificationStatsResponse } from '../gamification.service';
import { AdvancedStatsService, AdvancedStatsResponse } from '../services/advanced-stats.service';
import { ChartComponent } from '../components/chart/chart.component';

@Component({
  selector: 'app-admin-dashboard-enhanced',
  standalone: true,
  imports: [CommonModule, FormsModule, ChartComponent],
  template: `
    <div class="enhanced-dashboard">
      <!-- Header avec filtres temporels -->
      <div class="dashboard-header">
        <div class="header-content">
          <h1>Dashboard Administrateur</h1>
          <p>Analyse complète de la plateforme 9awiNiveau avec visualisations interactives</p>
        </div>
        <div class="header-controls">
          <div class="time-filter">
            <label>Période:</label>
            <select [(ngModel)]="selectedPeriod" (change)="onPeriodChange()" class="period-select">
              <option value="7d">7 derniers jours</option>
              <option value="30d">30 derniers jours</option>
              <option value="90d">3 derniers mois</option>
              <option value="1y">Dernière année</option>
            </select>
          </div>
          <button (click)="refreshData()" class="refresh-btn" [disabled]="loading">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" [class.spinning]="loading">
              <polyline points="23,4 23,10 17,10"></polyline>
              <polyline points="1,20 1,14 7,14"></polyline>
              <path d="M20.49 9A9 9 0 0 0 5.64 5.64L1 10m22 4l-4.64 4.36A9 9 0 0 1 3.51 15"></path>
            </svg>
            Actualiser
          </button>
        </div>
      </div>

      <!-- Messages -->
      <div *ngIf="error" class="alert alert-error">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="10"></circle>
          <line x1="15" y1="9" x2="9" y2="15"></line>
          <line x1="9" y1="9" x2="15" y2="15"></line>
        </svg>
        {{ error }}
      </div>

      <!-- KPIs principaux -->
      <div class="kpi-section">
        <div class="kpi-grid" *ngIf="userStats && gamificationStats">
          <div class="kpi-card primary">
            <div class="kpi-icon">👥</div>
            <div class="kpi-content">
              <div class="kpi-number">{{ userStats.totalUsers }}</div>
              <div class="kpi-label">Total Utilisateurs</div>
              <div class="kpi-trend positive">+12% ce mois</div>
            </div>
          </div>
          
          <div class="kpi-card success">
            <div class="kpi-icon">✅</div>
            <div class="kpi-content">
              <div class="kpi-number">{{ userStats.activeUsers }}</div>
              <div class="kpi-label">Utilisateurs Actifs</div>
              <div class="kpi-trend positive">+8% ce mois</div>
            </div>
          </div>
          
          <div class="kpi-card warning">
            <div class="kpi-icon">🏆</div>
            <div class="kpi-content">
              <div class="kpi-number">{{ gamificationStats.totalBadges }}</div>
              <div class="kpi-label">Badges Créés</div>
              <div class="kpi-trend neutral">Stable</div>
            </div>
          </div>
          
          <div class="kpi-card info">
            <div class="kpi-icon">⚡</div>
            <div class="kpi-content">
              <div class="kpi-number">{{ formatNumber(gamificationStats.totalXPAwarded) }}</div>
              <div class="kpi-label">XP Distribués</div>
              <div class="kpi-trend positive">+25% ce mois</div>
            </div>
          </div>
        </div>
      </div>

      <!-- Graphiques principaux -->
      <div class="charts-section" *ngIf="advancedStats">
        <!-- Première ligne de graphiques -->
        <div class="charts-row">
          <div class="chart-container">
            <div class="chart-header">
              <h3>📈 Croissance des Utilisateurs</h3>
              <p>Évolution sur les 30 derniers jours</p>
            </div>
            <app-chart 
              type="line" 
              [data]="userGrowthChartData" 
              [options]="lineChartOptions"
              height="300px">
            </app-chart>
          </div>
          
          <div class="chart-container">
            <div class="chart-header">
              <h3>🥧 Distribution des Rôles</h3>
              <p>Répartition par type d'utilisateur</p>
            </div>
            <app-chart 
              type="doughnut" 
              [data]="roleDistributionChartData" 
              [options]="doughnutChartOptions"
              height="300px">
            </app-chart>
          </div>
        </div>

        <!-- Deuxième ligne de graphiques -->
        <div class="charts-row">
          <div class="chart-container">
            <div class="chart-header">
              <h3>📊 Activité Hebdomadaire</h3>
              <p>Utilisateurs actifs par jour</p>
            </div>
            <app-chart 
              type="bar" 
              [data]="activityTrendChartData" 
              [options]="barChartOptions"
              height="300px">
            </app-chart>
          </div>
          
          <div class="chart-container">
            <div class="chart-header">
              <h3>🎯 Distribution des XP</h3>
              <p>Répartition des utilisateurs par niveau XP</p>
            </div>
            <app-chart 
              type="bar" 
              [data]="xpDistributionChartData" 
              [options]="barChartOptions"
              height="300px">
            </app-chart>
          </div>
        </div>

        <!-- Troisième ligne de graphiques -->
        <div class="charts-row">
          <div class="chart-container full-width">
            <div class="chart-header">
              <h3>🏅 Top Performers</h3>
              <p>Classement des 5 meilleurs utilisateurs</p>
            </div>
            <app-chart 
              type="bar" 
              [data]="topPerformersChartData" 
              [options]="horizontalBarChartOptions"
              height="250px">
            </app-chart>
          </div>
        </div>

        <!-- Quatrième ligne - Badges et Défis -->
        <div class="charts-row">
          <div class="chart-container">
            <div class="chart-header">
              <h3>🏆 Progression des Badges</h3>
              <p>Badges obtenus vs disponibles</p>
            </div>
            <app-chart 
              type="bar" 
              [data]="badgeProgressChartData" 
              [options]="stackedBarChartOptions"
              height="300px">
            </app-chart>
          </div>
          
          <div class="chart-container">
            <div class="chart-header">
              <h3>🎮 Taux de Completion des Défis</h3>
              <p>Défis terminés vs en cours</p>
            </div>
            <app-chart 
              type="pie" 
              [data]="challengeCompletionChartData" 
              [options]="pieChartOptions"
              height="300px">
            </app-chart>
          </div>
        </div>
      </div>

      <!-- Tableau de bord des actions -->
      <div class="actions-dashboard">
        <h2>🚀 Actions Rapides</h2>
        <div class="actions-grid">
          <a href="/admin/users" class="action-card users">
            <div class="action-icon">👥</div>
            <div class="action-content">
              <h3>Gérer Utilisateurs</h3>
              <p>{{ userStats?.totalUsers }} utilisateurs inscrits</p>
              <span class="action-badge">{{ userStats?.activeUsers }} actifs</span>
            </div>
          </a>
          
          <a href="/admin/gamification" class="action-card gamification">
            <div class="action-icon">🎮</div>
            <div class="action-content">
              <h3>Gamification</h3>
              <p>{{ gamificationStats?.totalBadges }} badges, {{ gamificationStats?.totalChallenges }} défis</p>
              <span class="action-badge">{{ gamificationStats?.totalBadgesEarned }} obtenus</span>
            </div>
          </a>
          
          <a href="/admin/analytics" class="action-card analytics">
            <div class="action-icon">📊</div>
            <div class="action-content">
              <h3>Rapports Avancés</h3>
              <p>Analyses détaillées et exports</p>
              <span class="action-badge">Bientôt</span>
            </div>
          </a>
          
          <a href="/admin/settings" class="action-card settings">
            <div class="action-icon">⚙️</div>
            <div class="action-content">
              <h3>Configuration</h3>
              <p>Paramètres système et sécurité</p>
              <span class="action-badge">Admin</span>
            </div>
          </a>
        </div>
      </div>
    </div>
  `,
  styleUrls: ['./admin-dashboard-enhanced.component.css']
})
export class AdminDashboardEnhancedComponent implements OnInit {
  userStats: UserStatsResponse | null = null;
  gamificationStats: GamificationStatsResponse | null = null;
  advancedStats: AdvancedStatsResponse | null = null;
  loading = false;
  error = '';
  selectedPeriod = '30d';

  // Données des graphiques
  userGrowthChartData: any;
  roleDistributionChartData: any;
  activityTrendChartData: any;
  xpDistributionChartData: any;
  topPerformersChartData: any;
  badgeProgressChartData: any;
  challengeCompletionChartData: any;

  // Options des graphiques
  lineChartOptions: any;
  doughnutChartOptions: any;
  barChartOptions: any;
  pieChartOptions: any;
  horizontalBarChartOptions: any;
  stackedBarChartOptions: any;

  constructor(
    private adminService: AdminService,
    private gamificationService: GamificationService,
    private advancedStatsService: AdvancedStatsService
  ) {
    this.initChartOptions();
  }

  ngOnInit() {
    this.loadData();
  }

  loadData() {
    this.loading = true;
    this.error = '';

    // Charger les statistiques de base
    this.adminService.getUserStats().subscribe({
      next: (stats) => {
        this.userStats = stats;
        this.checkLoadingComplete();
      },
      error: (error) => {
        console.error('Error loading user stats:', error);
        this.error = 'Erreur lors du chargement des statistiques utilisateurs';
        this.checkLoadingComplete();
      }
    });

    this.gamificationService.getGamificationStats().subscribe({
      next: (stats) => {
        this.gamificationStats = stats;
        this.checkLoadingComplete();
      },
      error: (error) => {
        console.error('Error loading gamification stats:', error);
        this.checkLoadingComplete();
      }
    });

    // Charger les statistiques avancées
    this.advancedStatsService.getAdvancedStats().subscribe({
      next: (stats) => {
        this.advancedStats = stats;
        this.prepareChartData();
        this.checkLoadingComplete();
      },
      error: (error) => {
        console.error('Error loading advanced stats:', error);
        this.error = 'Erreur lors du chargement des statistiques avancées';
        this.checkLoadingComplete();
      }
    });
  }

  private checkLoadingComplete() {
    if ((this.userStats !== null || this.error) && 
        (this.gamificationStats !== null || this.error) &&
        (this.advancedStats !== null || this.error)) {
      this.loading = false;
    }
  }

  refreshData() {
    this.loadData();
  }

  onPeriodChange() {
    this.loadData();
  }

  formatNumber(num: number): string {
    if (num >= 1000000) {
      return (num / 1000000).toFixed(1) + 'M';
    } else if (num >= 1000) {
      return (num / 1000).toFixed(1) + 'K';
    }
    return num.toString();
  }

  private initChartOptions() {
    this.lineChartOptions = {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          display: false
        }
      },
      scales: {
        y: {
          beginAtZero: true,
          grid: {
            color: 'rgba(0, 0, 0, 0.1)'
          }
        },
        x: {
          grid: {
            display: false
          }
        }
      }
    };

    this.doughnutChartOptions = {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: 'bottom' as const,
          labels: {
            padding: 20,
            usePointStyle: true
          }
        }
      }
    };

    this.barChartOptions = {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          display: false
        }
      },
      scales: {
        y: {
          beginAtZero: true
        }
      }
    };

    this.pieChartOptions = {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: 'bottom' as const
        }
      }
    };

    this.horizontalBarChartOptions = {
      responsive: true,
      maintainAspectRatio: false,
      indexAxis: 'y' as const,
      plugins: {
        legend: {
          display: false
        }
      },
      scales: {
        x: {
          beginAtZero: true
        }
      }
    };

    this.stackedBarChartOptions = {
      responsive: true,
      maintainAspectRatio: false,
      scales: {
        x: {
          stacked: true
        },
        y: {
          stacked: true,
          beginAtZero: true
        }
      }
    };
  }

  private prepareChartData() {
    if (!this.advancedStats) return;

    // Graphique de croissance des utilisateurs
    this.userGrowthChartData = this.advancedStatsService.convertToLineChartData(
      this.advancedStats.userGrowth,
      'Utilisateurs',
      '#063cdf'
    );

    // Distribution des rôles
    this.roleDistributionChartData = this.advancedStatsService.convertToPieChartData(
      this.advancedStats.roleDistribution
    );

    // Tendance d'activité
    this.activityTrendChartData = this.advancedStatsService.convertToLineChartData(
      this.advancedStats.activityTrend,
      'Utilisateurs Actifs',
      '#10b981'
    );

    // Distribution des XP
    this.xpDistributionChartData = this.advancedStatsService.convertToBarChartData(
      this.advancedStats.xpDistribution,
      'Utilisateurs',
      '#f59e0b'
    );

    // Top performers
    this.topPerformersChartData = {
      labels: this.advancedStats.topPerformers.map(p => p.name),
      datasets: [{
        label: 'XP',
        data: this.advancedStats.topPerformers.map(p => p.xp),
        backgroundColor: '#8b5cf6'
      }]
    };

    // Progression des badges
    this.badgeProgressChartData = {
      labels: this.advancedStats.badgeStats.map(b => b.name),
      datasets: [
        {
          label: 'Obtenus',
          data: this.advancedStats.badgeStats.map(b => b.obtained),
          backgroundColor: '#10b981'
        },
        {
          label: 'Restants',
          data: this.advancedStats.badgeStats.map(b => b.total - b.obtained),
          backgroundColor: '#e5e7eb'
        }
      ]
    };

    // Completion des défis
    this.challengeCompletionChartData = {
      labels: ['Terminés', 'En cours'],
      datasets: [{
        data: [
          this.advancedStats.challengeCompletion.completed,
          this.advancedStats.challengeCompletion.total - this.advancedStats.challengeCompletion.completed
        ],
        backgroundColor: ['#10b981', '#f59e0b']
      }]
    };
  }
}