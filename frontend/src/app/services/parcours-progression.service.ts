import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ApprenantProgression {
  userId: number;
  nom: string;
  prenom: string;
  email: string;
  dateInscription: string;
  dateCompletion?: string;
  progressionPourcentage: number;
  etapeCourante: number;
  totalEtapes: number;
  pointsGagnes: number;
  isCompleted: boolean;
  certificatGenere: boolean;
  certificatUrl?: string;
  statut: string;
  etapesProgression?: EtapeProgression[];
}

export interface EtapeProgression {
  etapeId: number;
  titreCours: string;
  ordreEtape: number;
  isCompleted: boolean;
  scoreObtenu: number;
  dateCompletion?: string;
}

export interface StatistiquesGlobales {
  totalInscrits: number;
  termines: number;
  enCours: number;
  certificats: number;
  progressionMoyenne: number;
}

@Injectable({
  providedIn: 'root'
})
export class ParcoursProgressionService {
  private apiUrl = 'http://localhost:8080/api/parcours';

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }

  getProgressionDetails(parcoursId: number): Observable<ApprenantProgression[]> {
    return this.http.get<ApprenantProgression[]>(
      `${this.apiUrl}/${parcoursId}/progression-details`,
      { headers: this.getHeaders() }
    );
  }

  getStatistiquesGlobales(parcoursId: number): Observable<StatistiquesGlobales> {
    return this.http.get<StatistiquesGlobales>(
      `${this.apiUrl}/${parcoursId}/statistiques-globales`,
      { headers: this.getHeaders() }
    );
  }
}