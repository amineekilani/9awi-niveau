import { Component, OnInit, OnDestroy, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavbarComponent } from '../navbar/navbar.component';
import { FormsModule } from '@angular/forms';
import { RecommendationService, Recommendation, RecommendationResponse } from '../recommendation.service';
import { Router } from '@angular/router';
import { interval, Subscription } from 'rxjs';

declare const feather: any;

/**
 * Composant d'affichage des recommandations pédagogiques
 * Interface utilisateur pour l'agent IA de recommandation
 */
@Component({
  selector: 'app-recommendations',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent],
  templateUrl: './recommendations.component.html',
  styleUrls: ['./recommendations.component.css']
})
export class RecommendationsComponent implements OnInit, OnDestroy, AfterViewInit {

  recommendations: Recommendation[] = [];
  groupedRecommendations: Map<string, Recommendation[]> = new Map();
  isLoading = false;
  error: string | null = null;
  encouragementMessage = '';

  // Filtres et options
  selectedType: string = 'ALL';
  minConfidence: number = 0.5;
  showOnlyHighPriority = false;

  // Auto-refresh
  private autoRefreshSubscription?: Subscription;
  private readonly AUTO_REFRESH_INTERVAL = 5 * 60 * 1000; // 5 minutes
  autoRefreshEnabled = true;
  lastUpdateTime: Date = new Date();

  // Types de recommandations disponibles
  recommendationTypes = [
    { value: 'ALL', label: 'Toutes les recommandations' },
    { value: 'COURS', label: 'Cours' },
    { value: 'LECON', label: 'Leçons' },
    { value: 'QUIZ', label: 'Quiz' },
    { value: 'CHALLENGE', label: 'Défis' }
  ];

  constructor(
    private recommendationService: RecommendationService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.loadRecommendations();
    this.setupAutoRefresh();
  }

  ngAfterViewInit(): void {
    if (typeof feather !== 'undefined') {
      setTimeout(() => feather.replace(), 100);
    }
  }

  ngOnDestroy(): void {
    this.stopAutoRefresh();
  }

  /**
   * Configure l'actualisation automatique
   */
  private setupAutoRefresh(): void {
    if (this.autoRefreshEnabled) {
      this.autoRefreshSubscription = interval(this.AUTO_REFRESH_INTERVAL).subscribe(() => {
        console.log('Auto-refresh des recommandations');
        this.loadRecommendations(true); // true = silent refresh
      });
    }
  }

  /**
   * Arrête l'actualisation automatique
   */
  private stopAutoRefresh(): void {
    if (this.autoRefreshSubscription) {
      this.autoRefreshSubscription.unsubscribe();
      this.autoRefreshSubscription = undefined;
    }
  }

  /**
   * Active/désactive l'auto-refresh
   */
  toggleAutoRefresh(): void {
    this.autoRefreshEnabled = !this.autoRefreshEnabled;

    if (this.autoRefreshEnabled) {
      this.setupAutoRefresh();
      console.log('Auto-refresh activé');
    } else {
      this.stopAutoRefresh();
      console.log('Auto-refresh désactivé');
    }
  }

  /**
   * Charge les recommandations depuis l'API
   */
  loadRecommendations(silent: boolean = false): void {
    if (!silent) {
      this.isLoading = true;
    }
    this.error = null;

    this.recommendationService.getMyRecommendations().subscribe({
      next: (response: RecommendationResponse) => {
        const hasChanges = this.hasRecommendationsChanged(response.recommendations);

        this.recommendations = response.recommendations;
        this.applyFilters();
        this.encouragementMessage = this.recommendationService.generateEncouragementMessage(this.recommendations);
        this.lastUpdateTime = new Date();
        this.isLoading = false;

        if (hasChanges && silent) {
          console.log('Nouvelles recommandations détectées lors de l\'auto-refresh');
          // Optionnel : afficher une notification discrète
        }

        // Rafraîchir les icônes SVG
        setTimeout(() => {
          if (typeof feather !== 'undefined') {
            feather.replace();
          }
        }, 100);
      },
      error: (error) => {
        console.error('Erreur lors du chargement des recommandations:', error);
        if (!silent) {
          this.error = 'Impossible de charger les recommandations. Veuillez réessayer.';
          this.isLoading = false;
          alert('Erreur lors du chargement des recommandations');
        }
      }
    });
  }

  /**
   * Vérifie si les recommandations ont changé
   */
  private hasRecommendationsChanged(newRecommendations: Recommendation[]): boolean {
    if (this.recommendations.length !== newRecommendations.length) {
      return true;
    }

    // Comparaison simple par ID et titre
    const currentIds = this.recommendations.map(r => `${r.id}-${r.type}`).sort();
    const newIds = newRecommendations.map(r => `${r.id}-${r.type}`).sort();

    return JSON.stringify(currentIds) !== JSON.stringify(newIds);
  }

  /**
   * Applique les filtres aux recommandations
   */
  applyFilters(): void {
    let filtered = [...this.recommendations];

    // Filtre par type
    if (this.selectedType !== 'ALL') {
      filtered = filtered.filter(rec => rec.type === this.selectedType);
    }

    // Filtre par confiance minimale
    filtered = this.recommendationService.filterByConfidence(filtered, this.minConfidence);

    // Filtre haute priorité uniquement
    if (this.showOnlyHighPriority) {
      filtered = filtered.filter(rec => rec.priority <= 2);
    }

    // Trier les recommandations
    filtered = this.recommendationService.sortRecommendations(filtered);

    // Grouper par type
    this.groupedRecommendations = this.recommendationService.groupRecommendationsByType(filtered);
  }

  /**
   * Gestionnaire de changement de filtre
   */
  onFilterChange(): void {
    this.applyFilters();
  }

  /**
   * Navigue vers le contenu recommandé
   */
  navigateToRecommendation(recommendation: Recommendation): void {
    switch (recommendation.type) {
      case 'COURS':
        this.router.navigate(['/cours', recommendation.id]);
        break;
      case 'LECON':
        // Naviguer vers la leçon (nécessite de récupérer l'ID du cours/module)
        alert('Navigation vers la leçon en cours de développement');
        break;
      case 'QUIZ':
        this.router.navigate(['/quiz', recommendation.id]);
        break;
      case 'CHALLENGE':
        this.router.navigate(['/challenges', recommendation.id]);
        break;
      default:
        alert('Type de recommandation non supporté');
    }
  }

  /**
   * Marque une recommandation comme vue/ignorée
   */
  dismissRecommendation(recommendation: Recommendation): void {
    // TODO: Implémenter la logique de dismissal côté backend
    this.recommendations = this.recommendations.filter(r => r.id !== recommendation.id);
    this.applyFilters();

    if (confirm('Recommandation masquée. Voulez-vous la restaurer ?')) {
      // Restaurer la recommandation
      this.loadRecommendations();
    }
  }

  /**
   * Actualise les recommandations
   */
  refreshRecommendations(): void {
    this.loadRecommendations();
    this.lastUpdateTime = new Date();
    console.log('Recommandations actualisées manuellement');
  }

  /**
   * Obtient le temps écoulé depuis la dernière mise à jour
   */
  getTimeSinceLastUpdate(): string {
    const now = new Date();
    const diffMs = now.getTime() - this.lastUpdateTime.getTime();
    const diffMinutes = Math.floor(diffMs / 60000);

    if (diffMinutes < 1) {
      return 'À l\'instant';
    } else if (diffMinutes < 60) {
      return `Il y a ${diffMinutes} min`;
    } else {
      const diffHours = Math.floor(diffMinutes / 60);
      return `Il y a ${diffHours}h`;
    }
  }

  /**
   * Obtient l'icône pour un type de recommandation
   */
  getRecommendationIcon(type: string): string {
    return this.recommendationService.getRecommendationIcon(type);
  }

  /**
   * Obtient la couleur de priorité
   */
  getPriorityColor(priority: number): string {
    return this.recommendationService.getPriorityColor(priority);
  }

  /**
   * Obtient le label de priorité
   */
  getPriorityLabel(priority: number): string {
    return this.recommendationService.getPriorityLabel(priority);
  }

  /**
   * Formate le score de confiance
   */
  formatConfidenceScore(score: number): string {
    return this.recommendationService.formatConfidenceScore(score);
  }

  /**
   * Détermine si une recommandation est hautement recommandée
   */
  isHighlyRecommended(recommendation: Recommendation): boolean {
    return this.recommendationService.isHighlyRecommended(recommendation);
  }

  /**
   * Obtient les clés du Map pour l'itération dans le template
   */
  getGroupKeys(): string[] {
    return Array.from(this.groupedRecommendations.keys());
  }

  /**
   * Obtient les recommandations pour un type donné
   */
  getRecommendationsForType(type: string): Recommendation[] {
    return this.groupedRecommendations.get(type) || [];
  }

  /**
   * Obtient le label français pour un type de recommandation
   */
  getTypeLabel(type: string): string {
    const typeMap: { [key: string]: string } = {
      'COURS': 'Cours recommandés',
      'LECON': 'Leçons suggérées',
      'QUIZ': 'Quiz à tenter',
      'CHALLENGE': 'Défis à relever'
    };
    return typeMap[type] || type;
  }

  /**
   * Charge des recommandations personnalisées
   */
  loadCustomRecommendations(): void {
    this.isLoading = true;

    const params = {
      maxRecommendations: 15,
      includeCompleted: false,
      focusArea: this.selectedType !== 'ALL' ? this.selectedType : undefined
    };

    this.recommendationService.getCustomRecommendations(params).subscribe({
      next: (response: RecommendationResponse) => {
        this.recommendations = response.recommendations;
        this.applyFilters();
        this.isLoading = false;
        console.log('Recommandations personnalisées chargées');
      },
      error: (error) => {
        console.error('Erreur lors du chargement des recommandations personnalisées:', error);
        this.isLoading = false;
        alert('Erreur lors du chargement des recommandations personnalisées');
      }
    });
  }
}