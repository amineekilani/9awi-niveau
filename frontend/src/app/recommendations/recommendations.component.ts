import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { NavbarComponent } from '../navbar/navbar.component';
import { AuthService } from '../auth';
import { RecommendationService, ParcoursRecommendation, CoursRecommendation } from '../recommendation.service';
import { ParcoursService } from '../parcours.service';
import { NiveauDifficulte } from '../parcours.service';

declare const feather: any;

@Component({
  selector: 'app-recommendations',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, NavbarComponent],
  templateUrl: './recommendations.component.html',
  styleUrls: ['./recommendations.component.css']
})
export class RecommendationsComponent implements OnInit {
  recommendations: ParcoursRecommendation[] = [];
  coursRecommendations: CoursRecommendation[] = [];
  loading = false;
  error = '';
  success = '';

  // Onglets
  activeTab = 'personalized'; // personalized, cours

  // Enum pour le template
  NiveauDifficulte = NiveauDifficulte;

  constructor(
    private recommendationService: RecommendationService,
    private parcoursService: ParcoursService,
    public authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    this.loadPersonalizedRecommendations();

    // Initialiser Feather icons
    setTimeout(() => {
      if (typeof feather !== 'undefined') {
        feather.replace();
      }
    }, 100);
  }

  switchTab(tab: string) {
    this.activeTab = tab;
    this.error = '';
    this.success = '';

    if (tab === 'personalized') {
      this.loadPersonalizedRecommendations();
    } else if (tab === 'cours') {
      this.loadPersonalizedCoursRecommendations();
    }

    // Réinitialiser les icônes
    setTimeout(() => {
      if (typeof feather !== 'undefined') {
        feather.replace();
      }
    }, 100);
  }

  loadPersonalizedRecommendations() {
    this.loading = true;
    this.error = '';

    this.recommendationService.getPersonalizedRecommendations(8).subscribe({
      next: (recommendations) => {
        this.recommendations = recommendations;
        this.loading = false;
        
        if (recommendations.length === 0) {
          this.error = 'Aucune recommandation disponible. Suivez des cours pour obtenir des suggestions personnalisées.';
        }

        setTimeout(() => {
          if (typeof feather !== 'undefined') {
            feather.replace();
          }
        }, 100);
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des recommandations';
        this.loading = false;
        console.error('Erreur:', err);
      }
    });
  }

  loadPersonalizedCoursRecommendations() {
    this.loading = true;
    this.error = '';

    this.recommendationService.getPersonalizedCoursRecommendations(8).subscribe({
      next: (recommendations) => {
        this.coursRecommendations = recommendations;
        this.loading = false;
        
        if (recommendations.length === 0) {
          this.error = 'Aucune recommandation de cours disponible. Suivez des cours pour obtenir des suggestions personnalisées.';
        }

        setTimeout(() => {
          if (typeof feather !== 'undefined') {
            feather.replace();
          }
        }, 100);
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des recommandations de cours';
        this.loading = false;
        console.error('Erreur:', err);
      }
    });
  }

  // Actions sur les parcours
  voirParcours(parcoursId: number) {
    this.router.navigate(['/parcours', parcoursId]);
  }

  sInscrireAuParcours(parcoursId: number) {
    this.parcoursService.sInscrireAuParcours(parcoursId).subscribe({
      next: (response) => {
        this.success = 'Inscription réussie ! Vous pouvez maintenant commencer le parcours.';
        
        // Mettre à jour le statut d'inscription dans les recommandations
        const recommendation = this.recommendations.find(r => r.id === parcoursId);
        if (recommendation) {
          recommendation.isInscrit = true;
        }
      },
      error: (err) => {
        this.error = 'Erreur lors de l\'inscription au parcours';
        console.error('Erreur:', err);
      }
    });
  }

  // Actions sur les cours
  voirCours(coursId: number) {
    this.router.navigate(['/cours', coursId]);
  }

  sInscrireAuCours(coursId: number) {
    this.router.navigate(['/cours', coursId]);
  }

  // Utilitaires
  getScoreColor(score: number): string {
    if (score >= 80) return 'text-green-600';
    if (score >= 60) return 'text-blue-600';
    if (score >= 40) return 'text-yellow-600';
    return 'text-gray-600';
  }

  getMatchBadgeClass(niveau: string): string {
    switch (niveau) {
      case 'PARFAIT': return 'bg-green-100 text-green-800';
      case 'BON': return 'bg-blue-100 text-blue-800';
      case 'ACCEPTABLE': return 'bg-yellow-100 text-yellow-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  }

  getDifficultyColor(niveau?: NiveauDifficulte): string {
    switch (niveau) {
      case NiveauDifficulte.DEBUTANT: return 'text-green-600';
      case NiveauDifficulte.INTERMEDIAIRE: return 'text-blue-600';
      case NiveauDifficulte.AVANCE: return 'text-orange-600';
      case NiveauDifficulte.EXPERT: return 'text-red-600';
      default: return 'text-gray-600';
    }
  }
}
