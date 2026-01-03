import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';

export interface Recommendation {
  type: 'LECON' | 'QUIZ' | 'CHALLENGE' | 'COURS';
  id: number;
  title: string;
  reason: string;
  priority: number;
  confidenceScore: number;
}

export interface RecommendationResponse {
  userId: number;
  generatedAt: string;
  recommendations: Recommendation[];
}

export interface CustomRecommendationParams {
  maxRecommendations?: number;
  includeCompleted?: boolean;
  focusArea?: string;
}

/**
 * Service Angular pour l'API de recommandations pédagogiques
 * Intègre l'agent IA de recommandation avec l'interface utilisateur
 */
@Injectable({
  providedIn: 'root'
})
export class RecommendationService {
  private apiUrl = `${environment.apiUrl}/recommendations`;

  constructor(private http: HttpClient) { }

  /**
   * Récupère les recommandations personnalisées pour l'utilisateur connecté
   */
  getMyRecommendations(): Observable<RecommendationResponse> {
    return this.http.get<RecommendationResponse>(`${this.apiUrl}/me`);
  }

  /**
   * Récupère les recommandations pour un utilisateur spécifique (admin/formateur)
   */
  getUserRecommendations(userId: number): Observable<RecommendationResponse> {
    return this.http.get<RecommendationResponse>(`${this.apiUrl}/user/${userId}`);
  }

  /**
   * Récupère des recommandations personnalisées avec paramètres
   */
  getCustomRecommendations(params: CustomRecommendationParams = {}): Observable<RecommendationResponse> {
    let httpParams = new HttpParams();

    if (params.maxRecommendations) {
      httpParams = httpParams.set('maxRecommendations', params.maxRecommendations.toString());
    }

    if (params.includeCompleted !== undefined) {
      httpParams = httpParams.set('includeCompleted', params.includeCompleted.toString());
    }

    if (params.focusArea) {
      httpParams = httpParams.set('focusArea', params.focusArea);
    }

    return this.http.get<RecommendationResponse>(`${this.apiUrl}/me/custom`, { params: httpParams });
  }

  /**
   * Test du moteur de recommandation (admin uniquement)
   */
  testRecommendationEngine(): Observable<any> {
    return this.http.get(`${this.apiUrl}/test`);
  }

  /**
   * Méthodes utilitaires pour l'interface utilisateur
   */

  /**
   * Groupe les recommandations par type
   */
  groupRecommendationsByType(recommendations: Recommendation[]): Map<string, Recommendation[]> {
    const grouped = new Map<string, Recommendation[]>();

    recommendations.forEach(rec => {
      if (!grouped.has(rec.type)) {
        grouped.set(rec.type, []);
      }
      grouped.get(rec.type)!.push(rec);
    });

    return grouped;
  }

  /**
   * Filtre les recommandations par niveau de confiance
   */
  filterByConfidence(recommendations: Recommendation[], minConfidence: number = 0.5): Recommendation[] {
    return recommendations.filter(rec => rec.confidenceScore >= minConfidence);
  }

  /**
   * Trie les recommandations par priorité et confiance
   */
  sortRecommendations(recommendations: Recommendation[]): Recommendation[] {
    return recommendations.sort((a, b) => {
      // Trier par priorité (1 = haute priorité)
      if (a.priority !== b.priority) {
        return a.priority - b.priority;
      }
      // Puis par score de confiance (décroissant)
      return b.confidenceScore - a.confidenceScore;
    });
  }

  /**
   * Obtient l'icône appropriée pour le type de recommandation
   * Utilise des emojis pour une compatibilité maximale et un design ludique
   */
  /**
   * Obtient l'icône appropriée pour le type de recommandation
   * Utilise des icônes Feather (SVG)
   */
  getRecommendationIcon(type: string): string {
    switch (type) {
      case 'COURS':
        return 'book';
      case 'LECON':
        return 'play-circle';
      case 'QUIZ':
        return 'check-square';
      case 'CHALLENGE':
        return 'award';
      default:
        return 'zap'; // Pour les autres types
    }
  }

  /**
   * Obtient la couleur appropriée pour le niveau de priorité
   * Retourne des couleurs modernes compatibles avec le thème sombre
   */
  getPriorityColor(priority: number): string {
    switch (priority) {
      case 1:
        return '#ef4444'; // Rouge vif (Tailwind red-500)
      case 2:
        return '#f97316'; // Orange (Tailwind orange-500)
      case 3:
        return '#3b82f6'; // Bleu (Tailwind blue-500)
      case 4:
        return '#22c55e'; // Vert (Tailwind green-500)
      default:
        return '#94a3b8'; // Gris (Tailwind slate-400)
    }
  }

  /**
   * Obtient le label de priorité en français
   */
  getPriorityLabel(priority: number): string {
    switch (priority) {
      case 1:
        return 'Priorité haute';
      case 2:
        return 'Priorité moyenne-haute';
      case 3:
        return 'Priorité moyenne';
      case 4:
        return 'Priorité basse';
      default:
        return 'Priorité inconnue';
    }
  }

  /**
   * Formate le score de confiance en pourcentage
   */
  formatConfidenceScore(score: number): string {
    return `${Math.round(score * 100)}%`;
  }

  /**
   * Détermine si une recommandation est "hautement recommandée"
   */
  isHighlyRecommended(recommendation: Recommendation): boolean {
    return recommendation.priority <= 2 && recommendation.confidenceScore >= 0.8;
  }

  /**
   * Génère un message d'encouragement basé sur les recommandations
   */
  generateEncouragementMessage(recommendations: Recommendation[]): string {
    if (recommendations.length === 0) {
      return "Excellent travail ! Vous êtes à jour avec vos apprentissages.";
    }

    const highPriorityCount = recommendations.filter(r => r.priority <= 2).length;
    const avgConfidence = recommendations.reduce((sum, r) => sum + r.confidenceScore, 0) / recommendations.length;

    if (highPriorityCount >= 3) {
      return "Plusieurs opportunités d'apprentissage vous attendent ! Commencez par les recommandations prioritaires.";
    } else if (avgConfidence >= 0.8) {
      return "Nos recommandations sont parfaitement adaptées à votre profil. C'est le moment idéal pour progresser !";
    } else {
      return "Continuez votre excellent parcours d'apprentissage avec ces suggestions personnalisées.";
    }
  }
}