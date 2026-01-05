import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';

export interface ParcoursRequest {
  titre: string;
  description?: string;
  thumbnailUrl?: string;
  categorie?: string;
  niveauDifficulte?: NiveauDifficulte;
  dureeEstimeeHeures?: number;
  prerequis?: string;
  typeParcours: TypeParcours;
  pointsBonus?: number;
  badgeCompletion?: string;
  certificatEnabled?: boolean;
  isPublished?: boolean;
  etapes?: ParcoursEtapeRequest[];
}

export interface ParcoursEtapeRequest {
  coursId: number;
  ordreEtape: number;
  niveauEtape?: number;
  isObligatoire?: boolean;
  scoreMinimum?: number;
  pourcentageCompletionRequis?: number;
  quizObligatoires?: boolean;
  description?: string;
}

export interface ParcoursResponse {
  id: number;
  titre: string;
  description?: string;
  thumbnailUrl?: string;
  categorie?: string;
  niveauDifficulte?: NiveauDifficulte;
  dureeEstimeeHeures?: number;
  prerequis?: string;
  typeParcours: TypeParcours;
  pointsBonus?: number;
  badgeCompletion?: string;
  certificatEnabled?: boolean;
  isPublished?: boolean;
  createdAt: string;
  updatedAt: string;
  formateurNom: string;
  formateurEmail: string;
  nombreEtapes: number;
  nombreInscriptions: number;
  nombreCompletions: number;
  progressionMoyenne?: number;
  etapes?: ParcoursEtapeResponse[];
  isInscrit?: boolean;
  progressionUtilisateur?: number;
  etapeCouranteUtilisateur?: number;
  // Champs supplémentaires pour les inscriptions
  dateInscription?: string;
  dateCompletion?: string;
  pointsGagnesUtilisateur?: number;
  isCompletedUtilisateur?: boolean;
  certificatGenere?: boolean;
  certificatUrl?: string;
}

export interface ParcoursEtapeResponse {
  id: number;
  coursId: number;
  coursTitle: string;
  coursDescription?: string;
  coursThumbnailUrl?: string;
  coursNiveauDifficulte?: NiveauDifficulte;
  coursCategorie?: string;
  ordreEtape: number;
  niveauEtape: number;
  isObligatoire: boolean;
  scoreMinimum: number;
  pourcentageCompletionRequis: number;
  quizObligatoires: boolean;
  description?: string;
  createdAt: string;
  isDebloque?: boolean;
  isComplete?: boolean;
  progressionCours?: number;
  scoreObtenu?: number;
}

export enum TypeParcours {
  LINEAIRE = 'LINEAIRE',
  FLEXIBLE = 'FLEXIBLE'
}

export enum NiveauDifficulte {
  DEBUTANT = 'DEBUTANT',
  INTERMEDIAIRE = 'INTERMEDIAIRE',
  AVANCE = 'AVANCE',
  EXPERT = 'EXPERT'
}

@Injectable({
  providedIn: 'root'
})
export class ParcoursService {
  private apiUrl = 'http://localhost:8080/api/parcours';

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    // Utiliser le service d'auth pour obtenir le token de manière cohérente
    const token = localStorage.getItem('auth-token');
    console.log('DEBUG Service: Token récupéré:', token ? 'Présent' : 'Absent');
    
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }

  // Créer un nouveau parcours
  createParcours(parcours: ParcoursRequest): Observable<ParcoursResponse> {
    return this.http.post<ParcoursResponse>(this.apiUrl, parcours, { headers: this.getHeaders() });
  }

  // Obtenir tous les parcours du formateur
  getMesParcours(): Observable<ParcoursResponse[]> {
    return this.http.get<ParcoursResponse[]>(`${this.apiUrl}/mes-parcours`, { headers: this.getHeaders() });
  }

  // Obtenir un parcours par ID
  getParcoursById(id: number): Observable<ParcoursResponse> {
    return this.http.get<ParcoursResponse>(`${this.apiUrl}/${id}`, { headers: this.getHeaders() });
  }

  // Mettre à jour un parcours
  updateParcours(id: number, parcours: ParcoursRequest): Observable<ParcoursResponse> {
    return this.http.put<ParcoursResponse>(`${this.apiUrl}/${id}`, parcours, { headers: this.getHeaders() });
  }

  // Supprimer un parcours
  deleteParcours(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`, { headers: this.getHeaders() });
  }

  // Publier/dépublier un parcours
  togglePublishParcours(id: number): Observable<ParcoursResponse> {
    const url = `${this.apiUrl}/${id}/toggle-publish`;
    const headers = this.getHeaders();
    
    console.log('DEBUG Service: URL appelée:', url);
    console.log('DEBUG Service: Headers:', headers);
    console.log('DEBUG Service: Token présent:', !!localStorage.getItem('auth-token'));
    
    return this.http.put<ParcoursResponse>(url, {}, { headers }).pipe(
      tap(response => console.log('DEBUG Service: Réponse reçue:', response)),
      catchError(error => {
        console.error('DEBUG Service: Erreur interceptée:', error);
        throw error;
      })
    );
  }

  // Ajouter une étape à un parcours
  addEtapeToParcours(parcoursId: number, etape: ParcoursEtapeRequest): Observable<ParcoursEtapeResponse> {
    return this.http.post<ParcoursEtapeResponse>(`${this.apiUrl}/${parcoursId}/etapes`, etape, { headers: this.getHeaders() });
  }

  // Obtenir les étapes d'un parcours
  getEtapesByParcours(parcoursId: number): Observable<ParcoursEtapeResponse[]> {
    return this.http.get<ParcoursEtapeResponse[]>(`${this.apiUrl}/${parcoursId}/etapes`, { headers: this.getHeaders() });
  }

  // Mettre à jour une étape
  updateEtape(etapeId: number, etape: ParcoursEtapeRequest): Observable<ParcoursEtapeResponse> {
    return this.http.put<ParcoursEtapeResponse>(`${this.apiUrl}/etapes/${etapeId}`, etape, { headers: this.getHeaders() });
  }

  // Supprimer une étape
  deleteEtape(etapeId: number): Observable<{message: string}> {
    return this.http.delete<{message: string}>(`${this.apiUrl}/etapes/${etapeId}`, { headers: this.getHeaders() });
  }

  // Réorganiser les étapes (drag & drop)
  reorderEtapes(parcoursId: number, nouvelOrdre: number[]): Observable<ParcoursEtapeResponse[]> {
    return this.http.put<ParcoursEtapeResponse[]>(`${this.apiUrl}/${parcoursId}/etapes/reorder`, nouvelOrdre, { headers: this.getHeaders() });
  }

  // Upload d'image pour parcours
  uploadParcoursImage(file: File): Observable<{filename: string}> {
    const formData = new FormData();
    formData.append('file', file);
    
    const token = localStorage.getItem('auth-token');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
      // Ne pas ajouter Content-Type pour FormData
    });
    
    return this.http.post<{filename: string}>(`${this.apiUrl}/upload-image`, formData, { headers });
  }

  // Utilitaires
  getNiveauDisplayName(niveau: NiveauDifficulte): string {
    const niveaux = {
      [NiveauDifficulte.DEBUTANT]: 'Débutant',
      [NiveauDifficulte.INTERMEDIAIRE]: 'Intermédiaire',
      [NiveauDifficulte.AVANCE]: 'Avancé',
      [NiveauDifficulte.EXPERT]: 'Expert'
    };
    return niveaux[niveau] || niveau;
  }

  getTypeParcoursDisplayName(type: TypeParcours): string {
    const types = {
      [TypeParcours.LINEAIRE]: 'Linéaire (séquentiel)',
      [TypeParcours.FLEXIBLE]: 'Flexible (libre)'
    };
    return types[type] || type;
  }

  getNiveauEtapeDisplayName(niveau: number): string {
    const niveaux: { [key: number]: string } = {
      1: 'Fondamental',
      2: 'Intermédiaire', 
      3: 'Avancé'
    };
    return niveaux[niveau] || `Niveau ${niveau}`;
  }

  // Obtenir les statistiques d'un parcours
  getStatistiquesParcours(id: number): Observable<ParcoursResponse> {
    return this.http.get<ParcoursResponse>(`${this.apiUrl}/${id}/statistiques`, { headers: this.getHeaders() });
  }

  // ===== MÉTHODES POUR LES APPRENANTS =====

  // Obtenir tous les parcours publiés
  getParcoursPublies(): Observable<ParcoursResponse[]> {
    return this.http.get<ParcoursResponse[]>(`${this.apiUrl}/publies`, { headers: this.getHeaders() });
  }

  // Rechercher des parcours publiés
  rechercherParcours(terme: string): Observable<ParcoursResponse[]> {
    return this.http.get<ParcoursResponse[]>(`${this.apiUrl}/rechercher?terme=${encodeURIComponent(terme)}`, { headers: this.getHeaders() });
  }

  // Obtenir les parcours par catégorie
  getParcoursParCategorie(categorie: string): Observable<ParcoursResponse[]> {
    return this.http.get<ParcoursResponse[]>(`${this.apiUrl}/categorie/${encodeURIComponent(categorie)}`, { headers: this.getHeaders() });
  }

  // Obtenir les parcours populaires
  getParcoursPopulaires(): Observable<ParcoursResponse[]> {
    return this.http.get<ParcoursResponse[]>(`${this.apiUrl}/populaires`, { headers: this.getHeaders() });
  }

  // S'inscrire à un parcours
  sInscrireAuParcours(parcoursId: number): Observable<{message: string}> {
    return this.http.post<{message: string}>(`${this.apiUrl}/${parcoursId}/inscription`, {}, { headers: this.getHeaders() });
  }

  // Se désinscrire d'un parcours
  seDesinscrireDuParcours(parcoursId: number): Observable<{message: string}> {
    return this.http.delete<{message: string}>(`${this.apiUrl}/${parcoursId}/inscription`, { headers: this.getHeaders() });
  }

  // Mettre à jour manuellement la progression d'un utilisateur
  recalculerProgression(): Observable<{message: string}> {
    return this.http.post<{message: string}>(`${this.apiUrl}/recalculer-progression`, {}, { headers: this.getHeaders() });
  }

  // Forcer la mise à jour de la progression d'un parcours spécifique
  forcerMiseAJourProgression(parcoursId: number): Observable<{message: string}> {
    return this.http.post<{message: string}>(`${this.apiUrl}/${parcoursId}/forcer-mise-a-jour`, {}, { headers: this.getHeaders() });
  }

  // NOUVEAU: Déclencher manuellement la mise à jour de progression
  triggerProgressionUpdate(parcoursId: number): Observable<{message: string}> {
    return this.http.post<{message: string}>(`${this.apiUrl}/${parcoursId}/trigger-progression-update`, {}, { headers: this.getHeaders() });
  }

  // Utilitaire pour construire l'URL complète de l'image
  getImageUrl(thumbnailUrl: string | undefined): string {
    if (!thumbnailUrl) return '';
    return `http://localhost:8080/images/parcours/${thumbnailUrl}`;
  }

  // Obtenir mes inscriptions aux parcours
  getMesInscriptions(): Observable<ParcoursResponse[]> {
    return this.http.get<ParcoursResponse[]>(`${this.apiUrl}/mes-inscriptions`, { headers: this.getHeaders() });
  }

  // Obtenir mes inscriptions en cours
  getMesInscriptionsEnCours(): Observable<ParcoursResponse[]> {
    return this.http.get<ParcoursResponse[]>(`${this.apiUrl}/mes-inscriptions/en-cours`, { headers: this.getHeaders() });
  }

  // Obtenir mes inscriptions terminées
  getMesInscriptionsTerminees(): Observable<ParcoursResponse[]> {
    return this.http.get<ParcoursResponse[]>(`${this.apiUrl}/mes-inscriptions/termines`, { headers: this.getHeaders() });
  }
}