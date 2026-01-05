import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { NiveauDifficulte } from './parcours.service';

export interface UserPreferences {
  id?: number;
  preferredCategories?: string; // JSON string
  preferredDifficulty?: NiveauDifficulte;
  learningStyle?: string;
  timeAvailabilityHours?: number;
  learningGoals?: string; // JSON string
  interests?: string; // JSON string
  careerFocus?: string;
  preferredDurationMin?: number;
  preferredDurationMax?: number;
  challengePreference?: string;
  certificationImportant?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface RecommendationRequest {
  preferredCategories?: string[];
  preferredDifficulty?: NiveauDifficulte;
  learningStyle?: string;
  timeAvailabilityHours?: number;
  learningGoals?: string[];
  interests?: string[];
  careerFocus?: string;
  preferredDurationMin?: number;
  preferredDurationMax?: number;
  challengePreference?: string;
  certificationImportant?: boolean;
  maxRecommendations?: number;
}

export interface ParcoursRecommendation {
  id: number;
  titre: string;
  description?: string;
  thumbnailUrl?: string;
  categorie?: string;
  niveauDifficulte?: NiveauDifficulte;
  dureeEstimeeHeures?: number;
  prerequis?: string;
  typeParcours: string;
  pointsBonus?: number;
  certificatEnabled?: boolean;
  formateurNom: string;
  nombreEtapes: number;
  nombreInscriptions: number;
  progressionMoyenne?: number;
  
  // Données de recommandation
  scoreRecommendation: number;
  raisonsRecommandation: string[];
  niveauCorrespondance: string;
  isInscrit?: boolean;
  progressionUtilisateur?: number;
  
  // Scores détaillés
  scoreCategorie?: number;
  scoreDifficulte?: number;
  scoreDuree?: number;
  scorePopularite?: number;
  scorePerformance?: number;
  scorePrerequisMatch?: number;
}

@Injectable({
  providedIn: 'root'
})
export class RecommendationService {
  private apiUrl = 'http://localhost:8080/api/recommendations';

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }

  /**
   * Obtenir des recommandations personnalisées
   */
  getPersonalizedRecommendations(maxResults: number = 5): Observable<ParcoursRecommendation[]> {
    return this.http.get<ParcoursRecommendation[]>(
      `${this.apiUrl}/personalized?maxResults=${maxResults}`,
      { headers: this.getHeaders() }
    ).pipe(
      tap(recommendations => {
        console.log('🤖 Recommandations personnalisées reçues:', recommendations.length);
      }),
      catchError(error => {
        console.error('❌ Erreur lors de la récupération des recommandations:', error);
        throw error;
      })
    );
  }

  /**
   * Obtenir des recommandations basées sur des critères
   */
  getRecommendationsByCriteria(criteria: RecommendationRequest): Observable<ParcoursRecommendation[]> {
    return this.http.post<ParcoursRecommendation[]>(
      `${this.apiUrl}/by-criteria`,
      criteria,
      { headers: this.getHeaders() }
    ).pipe(
      tap(recommendations => {
        console.log('🎯 Recommandations par critères reçues:', recommendations.length);
      }),
      catchError(error => {
        console.error('❌ Erreur lors de la récupération des recommandations par critères:', error);
        throw error;
      })
    );
  }

  /**
   * Obtenir des recommandations rapides (top 3)
   */
  getQuickRecommendations(): Observable<ParcoursRecommendation[]> {
    return this.http.get<ParcoursRecommendation[]>(
      `${this.apiUrl}/quick`,
      { headers: this.getHeaders() }
    ).pipe(
      tap(recommendations => {
        console.log('⚡ Recommandations rapides reçues:', recommendations.length);
      }),
      catchError(error => {
        console.error('❌ Erreur lors de la récupération des recommandations rapides:', error);
        throw error;
      })
    );
  }

  /**
   * Obtenir les préférences utilisateur
   */
  getUserPreferences(): Observable<UserPreferences> {
    return this.http.get<UserPreferences>(
      `${this.apiUrl}/preferences`,
      { headers: this.getHeaders() }
    ).pipe(
      tap(preferences => {
        console.log('👤 Préférences utilisateur récupérées');
      }),
      catchError(error => {
        console.error('❌ Erreur lors de la récupération des préférences:', error);
        throw error;
      })
    );
  }

  /**
   * Sauvegarder les préférences utilisateur
   */
  saveUserPreferences(preferences: UserPreferences): Observable<UserPreferences> {
    return this.http.post<UserPreferences>(
      `${this.apiUrl}/preferences`,
      preferences,
      { headers: this.getHeaders() }
    ).pipe(
      tap(savedPrefs => {
        console.log('💾 Préférences utilisateur sauvegardées');
      }),
      catchError(error => {
        console.error('❌ Erreur lors de la sauvegarde des préférences:', error);
        throw error;
      })
    );
  }

  /**
   * Utilitaires pour les préférences
   */
  parseJsonArray(jsonString?: string): string[] {
    if (!jsonString || jsonString.trim() === '') {
      return [];
    }
    try {
      return JSON.parse(jsonString);
    } catch (e) {
      console.error('Erreur parsing JSON:', e);
      return [];
    }
  }

  stringifyArray(array: string[]): string {
    if (!array || array.length === 0) {
      return '';
    }
    return JSON.stringify(array);
  }

  /**
   * Obtenir les catégories disponibles
   */
  getAvailableCategories(): string[] {
    return [
      'Programmation',
      'Web Development',
      'Mobile Development',
      'Data Science',
      'Intelligence Artificielle',
      'Cybersécurité',
      'DevOps',
      'Design UI/UX',
      'Base de données',
      'Cloud Computing',
      'Blockchain',
      'IoT',
      'Game Development',
      'Marketing Digital',
      'Gestion de projet'
    ];
  }

  /**
   * Obtenir les styles d'apprentissage disponibles
   */
  getLearningStyles(): { value: string, label: string }[] {
    return [
      { value: 'VISUAL', label: 'Visuel (diagrammes, images)' },
      { value: 'AUDITORY', label: 'Auditif (vidéos, podcasts)' },
      { value: 'KINESTHETIC', label: 'Kinesthésique (pratique, exercices)' },
      { value: 'READING', label: 'Lecture/Écriture (textes, notes)' }
    ];
  }

  /**
   * Obtenir les niveaux de défi disponibles
   */
  getChallengePreferences(): { value: string, label: string }[] {
    return [
      { value: 'LOW', label: 'Faible - Je préfère apprendre progressivement' },
      { value: 'MEDIUM', label: 'Moyen - J\'aime un défi raisonnable' },
      { value: 'HIGH', label: 'Élevé - Je veux être challengé au maximum' }
    ];
  }

  /**
   * Obtenir les objectifs d'apprentissage disponibles
   */
  getLearningGoals(): string[] {
    return [
      'Changer de carrière',
      'Améliorer mes compétences actuelles',
      'Obtenir une certification',
      'Créer mon propre projet',
      'Augmenter mon salaire',
      'Apprendre par passion',
      'Rester à jour avec les technologies',
      'Développer une expertise spécialisée',
      'Créer ma startup',
      'Enseigner à d\'autres'
    ];
  }

  /**
   * Obtenir les centres d'intérêt disponibles
   */
  getInterests(): string[] {
    return [
      'Applications web',
      'Applications mobiles',
      'Jeux vidéo',
      'Intelligence artificielle',
      'Analyse de données',
      'Sécurité informatique',
      'Automatisation',
      'Design d\'interface',
      'E-commerce',
      'Réseaux sociaux',
      'Fintech',
      'Healthtech',
      'Edtech',
      'Environnement',
      'Robotique'
    ];
  }

  /**
   * Obtenir les orientations professionnelles disponibles
   */
  getCareerFocuses(): string[] {
    return [
      'Développeur Frontend',
      'Développeur Backend',
      'Développeur Full-Stack',
      'Développeur Mobile',
      'Data Scientist',
      'DevOps Engineer',
      'Cybersecurity Specialist',
      'UI/UX Designer',
      'Product Manager',
      'Technical Lead',
      'Architect Solution',
      'Consultant IT',
      'Entrepreneur Tech',
      'Formateur/Enseignant',
      'Freelance'
    ];
  }
}