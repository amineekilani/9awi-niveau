import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';

export interface ExerciceSubmission {
  reponses: { [elementId: number]: string };
  tempsPasse: number;
}

export interface ElementResultat {
  elementId: number;
  contenu: string;
  reponseUtilisateur: string;
  reponseCorrecte: string;
  correct: boolean;
}

export interface ResultatExercice {
  id: number;
  userId: number;
  exerciceId: number;
  exerciceTitre: string;
  score: number;
  datePassed: number;
  nombreElements: number;
  reponsesCorrectes: number;
  tempsPasse: number;
  details: ElementResultat[];
}

export interface ExerciceAttempt {
  id: number;
  score: number;
  datePassed: number;
  reponsesCorrectes: number;
  nombreElements: number;
}

@Injectable({
  providedIn: 'root'
})
export class ExerciceResultatService {
  private apiUrl = `${environment.apiUrl}/exercice-resultats`;

  constructor(private http: HttpClient) { }

  submitExercice(exerciceId: number, submission: ExerciceSubmission): Observable<ResultatExercice> {
    return this.http.post<ResultatExercice>(`${this.apiUrl}/exercice/${exerciceId}/submit`, submission);
  }

  getUserExerciceAttempts(exerciceId: number): Observable<ExerciceAttempt[]> {
    return this.http.get<ExerciceAttempt[]>(`${this.apiUrl}/exercice/${exerciceId}/attempts`);
  }

  getBestScore(exerciceId: number): Observable<ExerciceAttempt> {
    return this.http.get<ExerciceAttempt>(`${this.apiUrl}/exercice/${exerciceId}/best-score`);
  }

  getResultatDetails(resultatId: number): Observable<ResultatExercice> {
    return this.http.get<ResultatExercice>(`${this.apiUrl}/${resultatId}`);
  }
}