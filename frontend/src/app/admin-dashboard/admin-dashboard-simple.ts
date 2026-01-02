import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="admin-dashboard">
      <div class="header">
        <div class="header-content">
          <h1>Administration des Utilisateurs</h1>
          <p>Gestion des comptes utilisateurs et des rôles</p>
        </div>
        <div class="header-actions">
          <a href="/admin/gamification" class="btn btn-gamification">
            <i class="icon-trophy"></i>
            Gamification
          </a>
        </div>
      </div>

      <!-- Messages -->
      <div *ngIf="error" class="alert alert-error">{{ error }}</div>
      <div *ngIf="success" class="alert alert-success">{{ success }}</div>
      <div *ngIf="loading" class="alert alert-info">Chargement...</div>

      <!-- Statistiques -->
      <div class="stats-grid">
        <div class="stat-card">
          <h3>{{ totalUsers }}</h3>
          <p>Total Utilisateurs</p>
        </div>
        <div class="stat-card">
          <h3>{{ activeUsers }}</h3>
          <p>Utilisateurs Actifs</p>
        </div>
        <div class="stat-card">
          <h3>{{ adminUsers }}</h3>
          <p>Administrateurs</p>
        </div>
        <div class="stat-card">
          <h3>{{ formateurUsers }}</h3>
          <p>Formateurs</p>
        </div>
        <div class="stat-card">
          <h3>{{ etudiantUsers }}</h3>
          <p>Étudiants</p>
        </div>
      </div>

      <!-- Interface simplifiée -->
      <div class="simple-interface">
        <h2>Interface Simplifiée</h2>
        <p>Cette interface fonctionne sans les dépendances Angular complètes.</p>
        <p>Pour une interface complète, installez les dépendances manquantes :</p>
        <code>npm install &#64;angular/forms &#64;angular/router</code>
        
        <div class="actions">
          <button (click)="loadStats()" class="btn btn-primary">Recharger les statistiques</button>
          <a href="/admin/gamification" class="btn btn-secondary">Aller à la Gamification</a>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .admin-dashboard {
      padding: 20px;
      max-width: 1200px;
      margin: 0 auto;
    }

    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 30px;
    }

    .header-content h1 {
      color: #2c3e50;
      margin-bottom: 8px;
      font-size: 2rem;
      font-weight: 600;
    }

    .header-content p {
      color: #7f8c8d;
      font-size: 1.1rem;
      margin: 0;
    }

    .btn-gamification {
      background: linear-gradient(135deg, #f39c12, #e67e22);
      color: white;
      padding: 12px 20px;
      border-radius: 8px;
      text-decoration: none;
      font-weight: 500;
      display: flex;
      align-items: center;
      gap: 8px;
      transition: all 0.3s ease;
    }

    .btn-gamification:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 16px rgba(243, 156, 18, 0.4);
    }

    .alert {
      padding: 12px 16px;
      border-radius: 6px;
      margin-bottom: 16px;
      font-weight: 500;
    }

    .alert-error {
      background-color: #f8d7da;
      color: #721c24;
      border: 1px solid #f5c6cb;
    }

    .alert-success {
      background-color: #d4edda;
      color: #155724;
      border: 1px solid #c3e6cb;
    }

    .alert-info {
      background-color: #d1ecf1;
      color: #0c5460;
      border: 1px solid #bee5eb;
    }

    .stats-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 20px;
      margin-bottom: 30px;
    }

    .stat-card {
      background: white;
      padding: 24px;
      border-radius: 12px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      text-align: center;
      border: 1px solid #ecf0f1;
    }

    .stat-card h3 {
      font-size: 2rem;
      font-weight: 700;
      color: #3498db;
      margin: 0 0 8px 0;
    }

    .stat-card p {
      color: #7f8c8d;
      margin: 0;
      font-weight: 500;
    }

    .simple-interface {
      background: white;
      padding: 30px;
      border-radius: 12px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      border: 1px solid #ecf0f1;
    }

    .simple-interface h2 {
      color: #2c3e50;
      margin-bottom: 16px;
    }

    .simple-interface p {
      color: #7f8c8d;
      margin-bottom: 12px;
      line-height: 1.6;
    }

    .simple-interface code {
      background: #f8f9fa;
      padding: 4px 8px;
      border-radius: 4px;
      font-family: monospace;
      color: #e74c3c;
    }

    .actions {
      margin-top: 20px;
      display: flex;
      gap: 12px;
      flex-wrap: wrap;
    }

    .btn {
      padding: 10px 20px;
      border: none;
      border-radius: 6px;
      font-weight: 500;
      text-decoration: none;
      cursor: pointer;
      transition: all 0.2s ease;
    }

    .btn-primary {
      background-color: #3498db;
      color: white;
    }

    .btn-primary:hover {
      background-color: #2980b9;
    }

    .btn-secondary {
      background-color: #95a5a6;
      color: white;
    }

    .btn-secondary:hover {
      background-color: #7f8c8d;
    }

    .icon-trophy::before {
      content: '🏆';
    }
  `]
})
export class AdminDashboardComponent implements OnInit {
  loading = false;
  error = '';
  success = '';
  
  // Stats simplifiées
  totalUsers = 0;
  activeUsers = 0;
  adminUsers = 0;
  formateurUsers = 0;
  etudiantUsers = 0;

  ngOnInit() {
    this.loadStats();
  }

  loadStats() {
    this.loading = true;
    this.error = '';
    
    // Simulation de chargement
    setTimeout(() => {
      this.totalUsers = 150;
      this.activeUsers = 142;
      this.adminUsers = 3;
      this.formateurUsers = 12;
      this.etudiantUsers = 135;
      this.loading = false;
      this.success = 'Statistiques chargées (données simulées)';
      setTimeout(() => this.success = '', 3000);
    }, 1000);
  }
}