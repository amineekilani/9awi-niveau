import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-simple-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="simple-dashboard">
      <div class="welcome-section">
        <h1>🎉 Bienvenue dans l'Administration</h1>
        <p>Tableau de bord administrateur de Kawi Niveau</p>
      </div>

      <div class="quick-actions">
        <h2>Actions Rapides</h2>
        <div class="actions-grid">
          <a routerLink="/admin/users" class="action-card">
            <div class="action-icon">👥</div>
            <h3>Gérer les Utilisateurs</h3>
            <p>Créer, modifier et gérer les comptes utilisateurs</p>
          </a>

          <a routerLink="/admin/gamification" class="action-card">
            <div class="action-icon">🏆</div>
            <h3>Gamification</h3>
            <p>Gérer les badges, défis et classements</p>
          </a>
        </div>
      </div>

      <div class="info-section">
        <h2>ℹ️ Information</h2>
        <p>
          Cette interface d'administration vous permet de gérer tous les aspects de votre plateforme d'apprentissage.
          Utilisez le menu de navigation à gauche pour accéder aux différentes sections.
        </p>
      </div>
    </div>
  `,
  styles: [`
    .simple-dashboard {
      max-width: 1200px;
      margin: 0 auto;
      padding: 20px;
    }

    .welcome-section {
      text-align: center;
      margin-bottom: 40px;
      padding: 40px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      border-radius: 16px;
    }

    .welcome-section h1 {
      font-size: 2.5rem;
      margin-bottom: 16px;
      font-weight: 700;
    }

    .welcome-section p {
      font-size: 1.2rem;
      opacity: 0.9;
    }

    .quick-actions {
      margin-bottom: 40px;
    }

    .quick-actions h2 {
      color: #2c3e50;
      margin-bottom: 24px;
      font-size: 1.5rem;
      font-weight: 600;
    }

    .actions-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
      gap: 24px;
    }

    .action-card {
      display: block;
      padding: 32px;
      background: white;
      border: 2px solid #ecf0f1;
      border-radius: 16px;
      text-decoration: none;
      color: inherit;
      transition: all 0.3s ease;
      text-align: center;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
    }

    .action-card:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
      border-color: #3498db;
    }

    .action-icon {
      font-size: 4rem;
      margin-bottom: 20px;
    }

    .action-card h3 {
      color: #2c3e50;
      margin: 0 0 12px 0;
      font-size: 1.3rem;
      font-weight: 600;
    }

    .action-card p {
      color: #7f8c8d;
      margin: 0;
      line-height: 1.6;
    }

    .info-section {
      background: white;
      padding: 32px;
      border-radius: 16px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
      border: 1px solid #ecf0f1;
    }

    .info-section h2 {
      color: #2c3e50;
      margin-bottom: 16px;
      font-size: 1.3rem;
      font-weight: 600;
    }

    .info-section p {
      color: #7f8c8d;
      line-height: 1.6;
      margin: 0;
    }

    @media (max-width: 768px) {
      .welcome-section h1 {
        font-size: 2rem;
      }
      
      .actions-grid {
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class SimpleDashboardComponent {}