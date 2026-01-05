import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ApprenantProgression {
  id: number;
  nom: string;
  email: string;
  dateInscription: string;
  progressionPourcentage: number;
  etapeCourante: number;
  nombreEtapesCompletes: number;
  totalEtapes: number;
  isCompleted: boolean;
  dateCompletion?: string;
  pointsGagnes: number;
  certificatGenere: boolean;
  certificatUrl?: string;
  tempsEcoule?: string;
  profileImage?: string;
}

export interface ParcoursProgressionStats {
  totalInscrits: number;
  termines: number;
  enCours: number;
  certificats: number;
}

@Injectable({
  providedIn: 'root'
})
export class ParcoursProgressionService {
  private apiUrl = 'http://localhost:8080/api/parcours';

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('auth-token');
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }

  /**
   * Récupère les détails de progression de tous les apprenants d'un parcours
   */
  getProgressionDetails(parcoursId: number): Observable<ApprenantProgression[]> {
    return this.http.get<ApprenantProgression[]>(
      `${this.apiUrl}/${parcoursId}/progression-details`, 
      { headers: this.getHeaders() }
    );
  }

  /**
   * Récupère les statistiques globales de progression d'un parcours
   */
  getProgressionStats(parcoursId: number): Observable<ParcoursProgressionStats> {
    return this.http.get<ParcoursProgressionStats>(
      `${this.apiUrl}/${parcoursId}/progression-stats`, 
      { headers: this.getHeaders() }
    );
  }
}