import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminService, UserStatsResponse } from '../admin.service';
import { GamificationService, GamificationStatsResponse } from '../gamification.service';

@Component({
  selector: 'app-admin-dashboard-main',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="modern-dashboard">
      <div class="dashboard-header">
        <div class="header-content">
          <h1>Dashboard Administrateur</h1>
          <p>Vue d'ensemble de la plateforme 9awiNiveau</p>
        </div>
        <div class="header-actions">
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

      <!-- Statistiques Utilisateurs -->
      <div class="stats-section">
        <h2>Statistiques Utilisateurs</h2>
        <div class="stats-grid" *ngIf="userStats">
          <div class="stat-card primary">
            <div class="stat-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                <circle cx="9" cy="7" r="4"></circle>
                <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
                <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
              </svg>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ userStats.totalUsers }}</div>
              <div class="stat-label">Total Utilisateurs</div>
            </div>
          </div>
          
          <div class="stat-card success">
            <div class="stat-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                <polyline points="22,4 12,14.01 9,11.01"></polyline>
              </svg>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ userStats.activeUsers }}</div>
              <div class="stat-label">Utilisateurs Actifs</div>
            </div>
          </div>
          
          <div class="stat-card admin">
            <div class="stat-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M12 1l3 6 6 3-6 3-3 6-3-6-6-3 6-3z"></path>
              </svg>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ userStats.adminUsers }}</div>
              <div class="stat-label">Administrateurs</div>
            </div>
          </div>
          
          <div class="stat-card formateur">
            <div class="stat-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"></path>
                <path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"></path>
              </svg>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ userStats.formateurUsers }}</div>
              <div class="stat-label">Formateurs</div>
            </div>
          </div>
          
          <div class="stat-card student">
            <div class="stat-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M22 10v6M2 10l10-5 10 5-10 5z"></path>
                <path d="M6 12v5c3 3 9 3 12 0v-5"></path>
              </svg>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ userStats.etudiantUsers }}</div>
              <div class="stat-label">Étudiants</div>
            </div>
          </div>
        </div>
      </div>

      <!-- Statistiques Gamification -->
      <div class="stats-section" *ngIf="gamificationStats">
        <h2>Statistiques Gamification</h2>
        <div class="stats-grid">
          <div class="stat-card trophy">
            <div class="stat-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polygon points="12,2 15.09,8.26 22,9.27 17,14.14 18.18,21.02 12,17.77 5.82,21.02 7,14.14 2,9.27 8.91,8.26"></polygon>
              </svg>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ gamificationStats.totalBadges }}</div>
              <div class="stat-label">Total Badges</div>
            </div>
          </div>
          
          <div class="stat-card challenge">
            <div class="stat-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14l-5-4.87 6.91-1.01L12 2z"></path>
              </svg>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ gamificationStats.totalChallenges }}</div>
              <div class="stat-label">Total Défis</div>
            </div>
          </div>
          
          <div class="stat-card xp">
            <div class="stat-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polygon points="12,2 15.09,8.26 22,9.27 17,14.14 18.18,21.02 12,17.77 5.82,21.02 7,14.14 2,9.27 8.91,8.26"></polygon>
              </svg>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ formatNumber(gamificationStats.totalXPAwarded) }}</div>
              <div class="stat-label">XP Attribués</div>
            </div>
          </div>
          
          <div class="stat-card average">
            <div class="stat-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="20" x2="18" y2="10"></line>
                <line x1="12" y1="20" x2="12" y2="4"></line>
                <line x1="6" y1="20" x2="6" y2="14"></line>
              </svg>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ Math.round(gamificationStats.averageUserXP) }}</div>
              <div class="stat-label">XP Moyen</div>
            </div>
          </div>
        </div>
      </div>

      <!-- Actions Rapides -->
      <div class="quick-actions">
        <h2>Actions Rapides</h2>
        <div class="actions-grid">
          <a href="/admin/users" class="action-card users">
            <div class="action-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                <circle cx="9" cy="7" r="4"></circle>
                <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
                <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
              </svg>
            </div>
            <div class="action-content">
              <h3>Gérer Utilisateurs</h3>
              <p>Créer, modifier et gérer les comptes utilisateurs</p>
            </div>
          </a>
          
          <a href="/admin/gamification" class="action-card gamification">
            <div class="action-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polygon points="12,2 15.09,8.26 22,9.27 17,14.14 18.18,21.02 12,17.77 5.82,21.02 7,14.14 2,9.27 8.91,8.26"></polygon>
              </svg>
            </div>
            <div class="action-content">
              <h3>Gamification</h3>
              <p>Gérer les badges, défis et classements</p>
            </div>
          </a>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .modern-dashboard {
      max-width: 1200px;
      margin: 0 auto;
      padding: 0;
    }

    .dashboard-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 2rem;
      padding: 2rem;
      background: white;
      border-radius: 12px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      border: 1px solid #e2e8f0;
    }

    .header-content h1 {
      color: #1e293b;
      font-size: 2.25rem;
      font-weight: 700;
      margin: 0 0 0.5rem 0;
      color: #063cdf;
    }

    .header-content p {
      color: #64748b;
      font-size: 1.1rem;
      margin: 0;
    }

    .refresh-btn {
      background-color: #063cdf;
      color: white;
      border: none;
      padding: 0.75rem 1.5rem;
      border-radius: 8px;
      cursor: pointer;
      display: flex;
      align-items: center;
      gap: 0.5rem;
      font-weight: 600;
      transition: all 0.2s ease;
    }

    .refresh-btn:hover {
      background-color: #0c4a6e;
      transform: translateY(-1px);
    }

    .refresh-btn:disabled {
      opacity: 0.7;
      cursor: not-allowed;
      transform: none;
    }

    .spinning {
      animation: spin 1s linear infinite;
    }

    @keyframes spin {
      from { transform: rotate(0deg); }
      to { transform: rotate(360deg); }
    }

    .stats-section {
      margin-bottom: 2rem;
    }

    .stats-section h2 {
      color: #1e293b;
      font-size: 1.5rem;
      font-weight: 600;
      margin-bottom: 1.5rem;
      padding-left: 0.5rem;
      border-left: 4px solid #063cdf;
    }

    .stats-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
      gap: 1.5rem;
      margin-bottom: 2rem;
    }

    .stat-card {
      background: white;
      border-radius: 8px;
      padding: 1.5rem;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
      display: flex;
      align-items: center;
      gap: 1rem;
      transition: all 0.2s ease;
      border: 1px solid #e2e8f0;
    }

    .stat-card:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      border-color: #063cdf;
    }

    .stat-icon {
      width: 50px;
      height: 50px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }

    .stat-card.primary .stat-icon { background-color: #063cdf; color: white; }
    .stat-card.success .stat-icon { background-color: #10b981; color: white; }
    .stat-card.admin .stat-icon { background-color: #f59e0b; color: white; }
    .stat-card.formateur .stat-icon { background-color: #8b5cf6; color: white; }
    .stat-card.student .stat-icon { background-color: #06b6d4; color: white; }
    .stat-card.trophy .stat-icon { background-color: #f97316; color: white; }
    .stat-card.challenge .stat-icon { background-color: #ec4899; color: white; }
    .stat-card.xp .stat-icon { background-color: #84cc16; color: white; }
    .stat-card.average .stat-icon { background-color: #6366f1; color: white; }

    .stat-content {
      flex: 1;
    }

    .stat-number {
      font-size: 2.5rem;
      font-weight: 800;
      color: #1e293b;
      line-height: 1;
      margin-bottom: 0.5rem;
    }

    .stat-label {
      color: #64748b;
      font-size: 0.875rem;
      font-weight: 600;
      text-transform: uppercase;
      letter-spacing: 0.05em;
    }

    .quick-actions h2 {
      color: #1e293b;
      font-size: 1.5rem;
      font-weight: 600;
      margin-bottom: 1.5rem;
      padding-left: 0.5rem;
      border-left: 4px solid #063cdf;
    }

    .actions-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
      gap: 1.5rem;
    }

    .action-card {
      background: white;
      border-radius: 8px;
      padding: 1.5rem;
      display: flex;
      align-items: center;
      gap: 1rem;
      text-decoration: none;
      color: inherit;
      transition: all 0.2s ease;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
      border: 1px solid #e2e8f0;
    }

    .action-card:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(6, 60, 223, 0.15);
      border-color: #063cdf;
    }

    .action-icon {
      width: 50px;
      height: 50px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #063cdf;
      color: white;
      flex-shrink: 0;
    }

    .action-content h3 {
      font-weight: 700;
      color: #1e293b;
      margin: 0 0 0.5rem 0;
      font-size: 1.25rem;
    }

    .action-content p {
      font-size: 0.875rem;
      color: #64748b;
      margin: 0;
      line-height: 1.5;
    }

    .alert {
      padding: 1rem 1.5rem;
      border-radius: 12px;
      margin-bottom: 1.5rem;
      display: flex;
      align-items: center;
      gap: 0.75rem;
      font-weight: 500;
    }

    .alert-error {
      background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%);
      color: #dc2626;
      border: 1px solid #fca5a5;
    }

    @media (max-width: 768px) {
      .dashboard-header {
        flex-direction: column;
        gap: 1rem;
        text-align: center;
      }

      .stats-grid {
        grid-template-columns: 1fr;
      }

      .actions-grid {
        grid-template-columns: 1fr;
      }

      .stat-card,
      .action-card {
        padding: 1.5rem;
      }
    }
  `]
})
export class AdminDashboardMainComponent implements OnInit {
  userStats: UserStatsResponse | null = null;
  gamificationStats: GamificationStatsResponse | null = null;
  loading = false;
  error = '';
  
  // Exposer Math pour le template
  Math = Math;

  constructor(
    private adminService: AdminService,
    private gamificationService: GamificationService
  ) {}

  ngOnInit() {
    this.loadData();
  }

  loadData() {
    this.loading = true;
    this.error = '';

    // Charger les statistiques utilisateurs
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

    // Charger les statistiques gamification
    this.gamificationService.getGamificationStats().subscribe({
      next: (stats) => {
        this.gamificationStats = stats;
        this.checkLoadingComplete();
      },
      error: (error) => {
        console.error('Error loading gamification stats:', error);
        // Ne pas afficher d'erreur si les tables gamification n'existent pas encore
        this.checkLoadingComplete();
      }
    });
  }

  private checkLoadingComplete() {
    if (this.userStats !== null || this.error) {
      this.loading = false;
    }
  }

  refreshData() {
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
}