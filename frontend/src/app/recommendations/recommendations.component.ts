import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { NavbarComponent } from '../navbar/navbar.component';
import { AuthService } from '../auth';
import { RecommendationService, ParcoursRecommendation, RecommendationRequest, UserPreferences } from '../recommendation.service';
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
  loading = false;
  error = '';
  success = '';

  // Onglets
  activeTab = 'personalized'; // personalized, criteria, preferences

  // Critères de recherche
  searchCriteria: RecommendationRequest = {
    maxRecommendations: 8
  };

  // Préférences utilisateur
  userPreferences: UserPreferences = {};
  preferencesLoading = false;
  preferencesSaving = false;

  // Options pour les formulaires
  availableCategories: string[] = [];
  learningStyles: { value: string, label: string }[] = [];
  challengePreferences: { value: string, label: string }[] = [];
  learningGoals: string[] = [];
  interests: string[] = [];
  careerFocuses: string[] = [];

  // Sélections multiples
  selectedCategories: string[] = [];
  selectedGoals: string[] = [];
  selectedInterests: string[] = [];

  // Enum pour le template
  NiveauDifficulte = NiveauDifficulte;

  constructor(
    private recommendationService: RecommendationService,
    private parcoursService: ParcoursService,
    public authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    this.initializeOptions();
    this.loadUserPreferences();
    this.loadPersonalizedRecommendations();

    // Initialiser Feather icons
    setTimeout(() => {
      if (typeof feather !== 'undefined') {
        feather.replace();
      }
    }, 100);
  }

  initializeOptions() {
    this.availableCategories = this.recommendationService.getAvailableCategories();
    this.learningStyles = this.recommendationService.getLearningStyles();
    this.challengePreferences = this.recommendationService.getChallengePreferences();
    this.learningGoals = this.recommendationService.getLearningGoals();
    this.interests = this.recommendationService.getInterests();
    this.careerFocuses = this.recommendationService.getCareerFocuses();
  }

  switchTab(tab: string) {
    this.activeTab = tab;
    this.error = '';
    this.success = '';

    if (tab === 'personalized') {
      this.loadPersonalizedRecommendations();
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
          this.error = 'Aucune recommandation disponible. Configurez vos préférences pour de meilleures suggestions.';
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

  searchByCriteria() {
    this.loading = true;
    this.error = '';

    // Préparer les critères
    const criteria: RecommendationRequest = {
      ...this.searchCriteria,
      preferredCategories: this.selectedCategories.length > 0 ? this.selectedCategories : undefined,
      learningGoals: this.selectedGoals.length > 0 ? this.selectedGoals : undefined,
      interests: this.selectedInterests.length > 0 ? this.selectedInterests : undefined
    };

    this.recommendationService.getRecommendationsByCriteria(criteria).subscribe({
      next: (recommendations) => {
        this.recommendations = recommendations;
        this.loading = false;
        
        if (recommendations.length === 0) {
          this.error = 'Aucun parcours ne correspond à vos critères. Essayez d\'élargir votre recherche.';
        } else {
          this.success = `${recommendations.length} recommandation(s) trouvée(s) selon vos critères.`;
        }

        setTimeout(() => {
          if (typeof feather !== 'undefined') {
            feather.replace();
          }
        }, 100);
      },
      error: (err) => {
        this.error = 'Erreur lors de la recherche par critères';
        this.loading = false;
        console.error('Erreur:', err);
      }
    });
  }

  loadUserPreferences() {
    this.preferencesLoading = true;

    this.recommendationService.getUserPreferences().subscribe({
      next: (preferences) => {
        this.userPreferences = preferences;
        
        // Parser les arrays JSON
        this.selectedCategories = this.recommendationService.parseJsonArray(preferences.preferredCategories);
        this.selectedGoals = this.recommendationService.parseJsonArray(preferences.learningGoals);
        this.selectedInterests = this.recommendationService.parseJsonArray(preferences.interests);
        
        this.preferencesLoading = false;
      },
      error: (err) => {
        console.error('Erreur lors du chargement des préférences:', err);
        this.preferencesLoading = false;
      }
    });
  }

  saveUserPreferences() {
    this.preferencesSaving = true;
    this.error = '';
    this.success = '';

    // Préparer les préférences avec les arrays JSON
    const preferencesToSave: UserPreferences = {
      ...this.userPreferences,
      preferredCategories: this.recommendationService.stringifyArray(this.selectedCategories),
      learningGoals: this.recommendationService.stringifyArray(this.selectedGoals),
      interests: this.recommendationService.stringifyArray(this.selectedInterests)
    };

    this.recommendationService.saveUserPreferences(preferencesToSave).subscribe({
      next: (savedPrefs) => {
        this.userPreferences = savedPrefs;
        this.success = 'Préférences sauvegardées avec succès !';
        this.preferencesSaving = false;
        
        // Recharger les recommandations personnalisées
        if (this.activeTab === 'personalized') {
          setTimeout(() => this.loadPersonalizedRecommendations(), 1000);
        }
      },
      error: (err) => {
        this.error = 'Erreur lors de la sauvegarde des préférences';
        this.preferencesSaving = false;
        console.error('Erreur:', err);
      }
    });
  }

  // Gestion des sélections multiples
  toggleCategory(category: string) {
    const index = this.selectedCategories.indexOf(category);
    if (index > -1) {
      this.selectedCategories.splice(index, 1);
    } else {
      this.selectedCategories.push(category);
    }
  }

  toggleGoal(goal: string) {
    const index = this.selectedGoals.indexOf(goal);
    if (index > -1) {
      this.selectedGoals.splice(index, 1);
    } else {
      this.selectedGoals.push(goal);
    }
  }

  toggleInterest(interest: string) {
    const index = this.selectedInterests.indexOf(interest);
    if (index > -1) {
      this.selectedInterests.splice(index, 1);
    } else {
      this.selectedInterests.push(interest);
    }
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

  clearCriteria() {
    this.searchCriteria = { maxRecommendations: 8 };
    this.selectedCategories = [];
    this.selectedGoals = [];
    this.selectedInterests = [];
    this.recommendations = [];
    this.error = '';
    this.success = '';
  }
}