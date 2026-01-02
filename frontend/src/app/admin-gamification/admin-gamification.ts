import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { GamificationService, GamificationStatsResponse } from '../gamification.service';
import { GamificationInitService } from '../gamification-init.service';
import { TestBackendService } from '../test-backend.service';
import { AuthService } from '../auth';
import { Router } from '@angular/router';
import { BadgeManagementComponent } from '../badge-management/badge-management';
import { ChallengeManagementComponent } from '../challenge-management/challenge-management';
import { LeaderboardManagementComponent } from '../leaderboard-management/leaderboard-management';

@Component({
  selector: 'app-admin-gamification',
  standalone: true,
  imports: [CommonModule, FormsModule, BadgeManagementComponent, ChallengeManagementComponent, LeaderboardManagementComponent],
  templateUrl: './admin-gamification.html',
  styleUrls: ['./admin-gamification.css']
})
export class AdminGamificationComponent implements OnInit {
  stats: GamificationStatsResponse | null = null;
  loading = false;
  error = '';
  
  activeTab: 'overview' | 'badges' | 'challenges' | 'leaderboard' = 'overview';

  constructor(
    private gamificationService: GamificationService,
    private gamificationInitService: GamificationInitService,
    private testBackendService: TestBackendService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    if (!this.authService.isAdmin()) {
      this.router.navigate(['/home']);
      return;
    }
    
    // Toujours permettre l'accès aux onglets, même si les stats ne se chargent pas
    this.loadStats();
  }

  loadStats() {
    this.loading = true;
    this.error = '';

    console.log('Loading gamification stats...');

    this.gamificationService.getGamificationStats().subscribe({
      next: (stats) => {
        console.log('Stats loaded successfully:', stats);
        this.stats = stats;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading gamification stats:', error);
        console.error('Error status:', error.status);
        console.error('Error message:', error.message);
        console.error('Error details:', error.error);
        
        // Message d'erreur plus informatif
        if (error.status === 404) {
          this.error = 'Endpoint non trouvé (404). Vérifiez que le contrôleur est bien configuré.';
        } else if (error.status === 403) {
          this.error = 'Accès interdit (403). Vérifiez vos permissions administrateur.';
        } else if (error.status === 500) {
          this.error = 'Erreur serveur (500). Détails: ' + (error.error?.message || 'Erreur inconnue');
        } else if (error.status === 0) {
          this.error = 'Impossible de contacter le serveur. Vérifiez que le backend est démarré.';
        } else {
          this.error = `Erreur ${error.status}: ${error.error?.message || error.message || 'Erreur inconnue'}`;
        }
        
        this.loading = false;
      }
    });
  }

  setActiveTab(tab: 'overview' | 'badges' | 'challenges' | 'leaderboard') {
    this.activeTab = tab;
    // Réinitialiser l'erreur quand on change d'onglet
    if (tab !== 'overview') {
      this.error = '';
    }
  }

  formatNumber(num: number): string {
    if (num >= 1000000) {
      return (num / 1000000).toFixed(1) + 'M';
    } else if (num >= 1000) {
      return (num / 1000).toFixed(1) + 'K';
    }
    return num.toString();
  }

  initializeGamification() {
    this.loading = true;
    this.error = '';

    this.gamificationInitService.initializeGamification().subscribe({
      next: (response: any) => {
        console.log('Gamification initialized:', response);
        // Recharger les statistiques après initialisation
        setTimeout(() => {
          this.loadStats();
        }, 1000);
      },
      error: (error) => {
        console.error('Error initializing gamification:', error);
        if (error.status === 403) {
          this.error = 'Accès non autorisé. Vous devez être administrateur.';
        } else if (error.status === 500) {
          this.error = 'Erreur serveur lors de l\'initialisation. Vérifiez la base de données.';
        } else {
          this.error = 'Erreur lors de l\'initialisation des données de gamification';
        }
        this.loading = false;
      }
    });
  }

  testBackend() {
    this.loading = true;
    this.error = '';

    this.testBackendService.testConnection().subscribe({
      next: (response: any) => {
        console.log('Backend test successful:', response);
        this.error = '✅ Backend connecté - Test des endpoints de gamification...';
        this.loading = false;
        
        // Tester spécifiquement les endpoints de gamification
        this.gamificationInitService.checkGamificationStatus().subscribe({
          next: (statusResponse: any) => {
            console.log('Gamification status:', statusResponse);
            if (statusResponse.tablesExist) {
              this.error = '✅ Tables de gamification trouvées - Initialisation des données...';
              setTimeout(() => {
                this.initializeGamification();
              }, 1000);
            } else {
              this.error = '⚠️ Tables de gamification manquantes - Exécutez la migration SQL d\'abord';
            }
          },
          error: (error) => {
            console.error('Gamification status check failed:', error);
            this.error = '❌ Endpoints de gamification non accessibles - Vérifiez la configuration du backend';
          }
        });
      },
      error: (error) => {
        console.error('Backend test failed:', error);
        this.error = '❌ Backend non accessible - Vérifiez que le serveur est démarré sur le port 8080';
        this.loading = false;
      }
    });
  }
}